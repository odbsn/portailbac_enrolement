"use client";

import { useRef, useState, useCallback } from "react";
import { Dialog } from "primereact/dialog";
import { ImportResult, useNouveauBachelierStore } from "../nouveauBachelierStore";
import { extractXlsxFromZips } from "../Zipextraction";

// ─── Types ────────────────────────────────────────────────────────────────────

type UploadState = "idle" | "extracting" | "uploading" | "success" | "error";

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatFileSize(bytes: number) {
  if (bytes < 1024) return `${bytes} o`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
}

function StatBadge({
  label,
  value,
  color,
}: {
  label: string;
  value: number;
  color: string;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        padding: "12px 18px",
        borderRadius: 10,
        background: color + "18",
        border: `1.5px solid ${color}40`,
        minWidth: 90,
      }}
    >
      <span
        style={{
          fontSize: 22,
          fontWeight: 700,
          color,
          fontVariantNumeric: "tabular-nums",
        }}
      >
        {value.toLocaleString()}
      </span>
      <span style={{ fontSize: 11, color: "#888", marginTop: 2 }}>{label}</span>
    </div>
  );
}

function FileResultCard({ result }: { result: ImportResult }) {
  const [expanded, setExpanded] = useState(false);
  const hasWarnings = (result.warnings?.length ?? 0) > 0;
  const hasIssues = result.juryIntrouvable > 0 || hasWarnings;

  return (
    <div
      style={{
        border: `1px solid ${hasIssues ? "#fecaca" : "#e5e7eb"}`,
        borderRadius: 10,
        marginBottom: 10,
        overflow: "hidden",
      }}
    >
      <div
        onClick={() => setExpanded((e) => !e)}
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "10px 14px",
          cursor: "pointer",
          background: hasIssues ? "#fef2f2" : "#f9fafb",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}>
          <span style={{ fontSize: 16 }}>{hasIssues ? "⚠️" : "✅"}</span>
          <span
            style={{
              fontWeight: 600,
              fontSize: 13,
              color: "#111",
              whiteSpace: "nowrap",
              overflow: "hidden",
              textOverflow: "ellipsis",
              maxWidth: 280,
            }}
            title={result.fichier}
          >
            {result.fichier}
          </span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10, flexShrink: 0 }}>
          <span style={{ fontSize: 12, color: "#6b7280" }}>
            {result.total} ligne{result.total > 1 ? "s" : ""}
          </span>
          <span style={{ fontSize: 13, color: "#9ca3af" }}>{expanded ? "▲" : "▼"}</span>
        </div>
      </div>

      {expanded && (
        <div style={{ padding: "12px 14px", borderTop: "1px solid #f0f0f0" }}>
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: hasWarnings ? 12 : 0 }}>
            <MiniStat label="Nouveaux" value={result.nouveaux} color="#22c55e" />
            <MiniStat label="Modifiés" value={result.modifies} color="#f59e0b" />
            <MiniStat label="Inchangés" value={result.inchanges} color="#6b7280" />
            <MiniStat label="Jury inconnu" value={result.juryIntrouvable} color="#ef4444" />
          </div>

          {hasWarnings && (
            <div
              style={{
                maxHeight: 160,
                overflowY: "auto",
                borderRadius: 8,
                border: "1px solid #f0f0f0",
              }}
            >
              <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
                <thead>
                  <tr style={{ background: "#fef2f2", position: "sticky", top: 0 }}>
                    <th style={{ padding: "6px 10px", textAlign: "left", color: "#ef4444", fontWeight: 600 }}>
                      Avertissement
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {result.warnings.map((w, i) => (
                    <tr key={i} style={{ borderTop: "1px solid #fef2f2" }}>
                      <td style={{ padding: "6px 10px", color: "#374151" }}>{w}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function MiniStat({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 6,
        padding: "5px 10px",
        borderRadius: 7,
        background: color + "15",
        fontSize: 12,
      }}
    >
      <span style={{ fontWeight: 700, color }}>{value}</span>
      <span style={{ color: "#6b7280" }}>{label}</span>
    </div>
  );
}

// ─── Component ────────────────────────────────────────────────────────────────

export default function ImportZipModal({ open, onClose, onSuccess }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [zipFiles, setZipFiles] = useState<File[]>([]);
  const [dragging, setDragging] = useState(false);
  const [state, setState] = useState<UploadState>("idle");
  const [progress, setProgress] = useState(0);
  const [results, setResults] = useState<ImportResult[] | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [extractionErrors, setExtractionErrors] = useState<string[]>([]);

  const importExcelMultiple = useNouveauBachelierStore((state) => state.importExcelMultiple);

  const isZipFile = (f: File) => f.name.toLowerCase().endsWith(".zip");

  const addFiles = (incoming: FileList | File[]) => {
    const valides = Array.from(incoming).filter(isZipFile);
    setZipFiles((prev) => {
      const existants = new Set(prev.map((f) => `${f.name}_${f.size}`));
      const nouveaux = valides.filter((f) => !existants.has(`${f.name}_${f.size}`));
      return [...prev, ...nouveaux];
    });
  };

  const removeFile = (index: number) => {
    setZipFiles((prev) => prev.filter((_, i) => i !== index));
  };

  // ── drag & drop ──
  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    if (e.dataTransfer.files?.length) addFiles(e.dataTransfer.files);
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.length) addFiles(e.target.files);
  };

  // ── extraction puis upload ──
  const handleUpload = async () => {
    if (zipFiles.length === 0) return;
    setResults(null);
    setErrorMsg(null);
    setExtractionErrors([]);

    // ── Étape 1 : extraction des .xlsx depuis chaque zip, en parallèle ──
    setState("extracting");
    setProgress(0);

    let extraction;
    try {
      extraction = await extractXlsxFromZips(zipFiles);
    } catch (e: any) {
      setState("error");
      setErrorMsg(e?.message || "Erreur lors de l'extraction des fichiers zip");
      return;
    }

    if (extraction.errors.length > 0) {
      setExtractionErrors(extraction.errors);
    }

    if (extraction.files.length === 0) {
      setState("error");
      setErrorMsg("Aucun fichier .xlsx n'a pu être extrait des zip fournis.");
      return;
    }

    // ── Étape 2 : upload des .xlsx extraits, comme un import multiple classique ──
    setState("uploading");
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 90) {
          clearInterval(interval);
          return 90;
        }
        return prev + 10;
      });
    }, 200);

    try {
      const data = await importExcelMultiple(extraction.files);
      clearInterval(interval);
      setProgress(100);
      setResults(data);
      setState("success");
      onSuccess?.();
    } catch (err: any) {
      clearInterval(interval);
      setState("error");
      setErrorMsg(
        err?.response?.data?.message || err?.message || "Erreur serveur inconnue"
      );
    }
  };

  const reset = () => {
    setZipFiles([]);
    setState("idle");
    setProgress(0);
    setResults(null);
    setErrorMsg(null);
    setExtractionErrors([]);
    if (inputRef.current) inputRef.current.value = "";
  };

  const totaux = (results ?? []).reduce(
    (acc, r) => ({
      nouveaux: acc.nouveaux + r.nouveaux,
      modifies: acc.modifies + r.modifies,
      inchanges: acc.inchanges + r.inchanges,
      juryIntrouvable: acc.juryIntrouvable + r.juryIntrouvable,
      total: acc.total + r.total,
    }),
    { nouveaux: 0, modifies: 0, inchanges: 0, juryIntrouvable: 0, total: 0 }
  );

  const totalSize = zipFiles.reduce((sum, f) => sum + f.size, 0);

  const dialogHeader = (
    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
      <div
        style={{
          width: 36, height: 36, borderRadius: 8,
          background: "#fff7ed", border: "1px solid #fed7aa",
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: 18,
        }}
      >
        🗜️
      </div>
      <div>
        <p style={{ margin: 0, fontWeight: 600, fontSize: 15, color: "#111" }}>
          Importer depuis des fichiers ZIP
        </p>
        <p style={{ margin: 0, fontSize: 12, color: "#888" }}>
          Le .xlsx de chaque zip est extrait automatiquement
        </p>
      </div>
    </div>
  );

  return (
    <Dialog
      visible={open}
      onHide={() => {
        reset();
        onClose();
      }}
      header={dialogHeader}
      modal
      style={{ width: "680px" }}
      contentStyle={{ padding: "20px 24px 24px" }}
      draggable={false}
      resizable={false}
    >
      <div>
        {/* ── IDLE / FILES SELECTED ── */}
        {state === "idle" && (
          <>
            {/* Drop zone */}
            <div
              onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
              onDragLeave={() => setDragging(false)}
              onDrop={handleDrop}
              onClick={() => inputRef.current?.click()}
              style={{
                border: `2px dashed ${dragging ? "#f97316" : zipFiles.length > 0 ? "#f97316" : "#e5e7eb"}`,
                borderRadius: 12,
                padding: "24px 20px",
                textAlign: "center",
                cursor: "pointer",
                background: dragging || zipFiles.length > 0 ? "#fff7ed" : "#fafafa",
                transition: "all .15s",
                marginBottom: 12,
              }}
            >
              <input
                ref={inputRef}
                type="file"
                accept=".zip"
                multiple
                style={{ display: "none" }}
                onChange={handleFileChange}
              />
              <div style={{ fontSize: 30, marginBottom: 6 }}>🗜️</div>
              <p style={{ margin: "0 0 4px", fontWeight: 500, color: "#374151", fontSize: 14 }}>
                Glissez vos fichiers .zip ici
              </p>
              <p style={{ margin: 0, fontSize: 12, color: "#9ca3af" }}>
                ou cliquez pour parcourir (plusieurs zip possibles)
              </p>
            </div>

            {/* Liste des fichiers sélectionnés */}
            {zipFiles.length > 0 && (
              <div
                style={{
                  marginBottom: 16,
                  border: "1px solid #e5e7eb",
                  borderRadius: 10,
                  maxHeight: 220,
                  overflowY: "auto",
                }}
              >
                {zipFiles.map((f, i) => (
                  <div
                    key={`${f.name}_${f.size}_${i}`}
                    style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      padding: "9px 12px",
                      borderBottom: i < zipFiles.length - 1 ? "1px solid #f0f0f0" : "none",
                    }}
                  >
                    <div style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}>
                      <span style={{ fontSize: 16 }}>🗜️</span>
                      <span
                        style={{
                          fontSize: 13,
                          color: "#111",
                          fontWeight: 500,
                          whiteSpace: "nowrap",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                          maxWidth: 360,
                        }}
                        title={f.name}
                      >
                        {f.name}
                      </span>
                      <span style={{ fontSize: 11, color: "#9ca3af" }}>{formatFileSize(f.size)}</span>
                    </div>
                    <button
                      onClick={() => removeFile(i)}
                      style={{
                        background: "none",
                        border: "none",
                        color: "#ef4444",
                        cursor: "pointer",
                        fontSize: 14,
                        padding: "2px 6px",
                      }}
                      aria-label="Retirer"
                    >
                      ✕
                    </button>
                  </div>
                ))}
                <div style={{ padding: "8px 12px", fontSize: 12, color: "#6b7280", background: "#f9fafb" }}>
                  {zipFiles.length} zip — {formatFileSize(totalSize)} au total
                </div>
              </div>
            )}

            {/* Actions */}
            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
              {zipFiles.length > 0 && (
                <button
                  onClick={reset}
                  style={{
                    padding: "9px 18px",
                    borderRadius: 8,
                    border: "1px solid #e5e7eb",
                    background: "#fff",
                    color: "#6b7280",
                    fontSize: 13,
                    cursor: "pointer",
                    fontWeight: 500,
                  }}
                >
                  Tout retirer
                </button>
              )}
              <button
                onClick={handleUpload}
                disabled={zipFiles.length === 0}
                style={{
                  padding: "9px 22px",
                  borderRadius: 8,
                  border: "none",
                  background: zipFiles.length > 0 ? "#f97316" : "#e5e7eb",
                  color: zipFiles.length > 0 ? "#fff" : "#9ca3af",
                  fontSize: 13,
                  cursor: zipFiles.length > 0 ? "pointer" : "not-allowed",
                  fontWeight: 600,
                  transition: "background .15s",
                }}
              >
                Extraire et importer ({zipFiles.length})
              </button>
            </div>
          </>
        )}

        {/* ── EXTRACTING ── */}
        {state === "extracting" && (
          <div style={{ textAlign: "center", padding: "8px 0 16px" }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>🗜️</div>
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#111", fontSize: 15 }}>
              Extraction des fichiers .xlsx…
            </p>
            <p style={{ margin: 0, fontSize: 12, color: "#6b7280" }}>
              {zipFiles.length} zip en cours de lecture dans le navigateur
            </p>
          </div>
        )}

        {/* ── UPLOADING ── */}
        {state === "uploading" && (
          <div style={{ textAlign: "center", padding: "8px 0 16px" }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#111", fontSize: 15 }}>
              Import en cours…
            </p>
            <p style={{ margin: "0 0 20px", fontSize: 12, color: "#6b7280" }}>
              Fichiers .xlsx extraits envoyés au serveur
            </p>

            <div
              style={{
                height: 8,
                borderRadius: 99,
                background: "#e5e7eb",
                overflow: "hidden",
                marginBottom: 8,
              }}
            >
              <div
                style={{
                  height: "100%",
                  width: `${progress}%`,
                  background:
                    progress < 40
                      ? "#3b82f6"
                      : progress < 100
                      ? "#8b5cf6"
                      : "#22c55e",
                  borderRadius: 99,
                  transition: "width .3s ease, background .5s",
                }}
              />
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", fontSize: 11, color: "#9ca3af" }}>
              <span>
                {progress < 40
                  ? "📡 Envoi des fichiers"
                  : progress < 100
                  ? "⚙️ Parsing parallèle + fusion serveur"
                  : "✅ Finalisation"}
              </span>
              <span>{progress}%</span>
            </div>
          </div>
        )}

        {/* ── ERROR ── */}
        {state === "error" && (
          <div
            style={{
              background: "#fef2f2",
              border: "1px solid #fecaca",
              borderRadius: 10,
              padding: "16px 20px",
              marginBottom: 16,
            }}
          >
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#dc2626", fontSize: 14 }}>
              ❌ Erreur lors de l'import
            </p>
            <p style={{ margin: 0, fontSize: 13, color: "#6b7280" }}>{errorMsg}</p>
            {extractionErrors.length > 0 && (
              <ul style={{ margin: "10px 0 0", paddingLeft: 18, fontSize: 12, color: "#991b1b" }}>
                {extractionErrors.map((e, i) => (
                  <li key={i}>{e}</li>
                ))}
              </ul>
            )}
            <button
              onClick={reset}
              style={{
                marginTop: 12,
                padding: "7px 16px",
                borderRadius: 7,
                border: "1px solid #fecaca",
                background: "#fff",
                color: "#dc2626",
                fontSize: 12,
                cursor: "pointer",
                fontWeight: 500,
              }}
            >
              Réessayer
            </button>
          </div>
        )}

        {/* ── SUCCESS + RESULTS ── */}
        {state === "success" && results && (
          <>
            {/* Avertissement si certains zip n'ont pas pu être traités */}
            {extractionErrors.length > 0 && (
              <div
                style={{
                  background: "#fffbeb",
                  border: "1px solid #fde68a",
                  borderRadius: 10,
                  padding: "10px 14px",
                  marginBottom: 14,
                }}
              >
                <p style={{ margin: "0 0 4px", fontWeight: 600, color: "#92400e", fontSize: 12 }}>
                  ⚠️ {extractionErrors.length} zip ignoré{extractionErrors.length > 1 ? "s" : ""} (aucun .xlsx trouvé)
                </p>
                <ul style={{ margin: 0, paddingLeft: 16, fontSize: 12, color: "#92400e" }}>
                  {extractionErrors.map((e, i) => (
                    <li key={i}>{e}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* Stats globales agrégées */}
            <div
              style={{
                display: "flex",
                gap: 10,
                flexWrap: "wrap",
                marginBottom: 16,
                justifyContent: "center",
              }}
            >
              <StatBadge label="Importés" value={totaux.nouveaux + totaux.modifies} color="#16a34a" />
              <StatBadge label="Nouveaux" value={totaux.nouveaux} color="#22c55e" />
              <StatBadge label="Modifiés" value={totaux.modifies} color="#f59e0b" />
              <StatBadge label="Inchangés" value={totaux.inchanges} color="#6b7280" />
              <StatBadge label="Jury inconnu" value={totaux.juryIntrouvable} color="#ef4444" />
            </div>

            <p style={{ margin: "0 0 10px", fontSize: 12, color: "#6b7280", textAlign: "center" }}>
              {results.length} fichier{results.length > 1 ? "s" : ""} extrait{results.length > 1 ? "s" : ""} et traité{results.length > 1 ? "s" : ""} — {totaux.total} ligne{totaux.total > 1 ? "s" : ""} au total
            </p>

            {/* Détail par fichier (accordéon) */}
            <div style={{ maxHeight: 320, overflowY: "auto", paddingRight: 2 }}>
              {results.map((r, i) => (
                <FileResultCard key={`${r.fichier}_${i}`} result={r} />
              ))}
            </div>

            {/* Footer actions */}
            <div
              style={{
                display: "flex",
                gap: 8,
                justifyContent: "flex-end",
                marginTop: 18,
              }}
            >
              <button
                onClick={reset}
                style={{
                  padding: "9px 18px",
                  borderRadius: 8,
                  border: "1px solid #e5e7eb",
                  background: "#fff",
                  color: "#374151",
                  fontSize: 13,
                  cursor: "pointer",
                  fontWeight: 500,
                }}
              >
                Nouvel import
              </button>
              <button
                onClick={() => {
                  reset();
                  onClose();
                }}
                style={{
                  padding: "9px 22px",
                  borderRadius: 8,
                  border: "none",
                  background: "#f97316",
                  color: "#fff",
                  fontSize: 13,
                  cursor: "pointer",
                  fontWeight: 600,
                }}
              >
                Fermer
              </button>
            </div>
          </>
        )}
      </div>
    </Dialog>
  );
}
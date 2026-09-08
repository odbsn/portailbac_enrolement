"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Dialog } from "primereact/dialog";
import {
  DiplomeImportJob,
  DiplomeImportResult,
  useNouveauBachelierStore,
} from "../nouveauBachelierStore";

// ─── Types ────────────────────────────────────────────────────────────────────

type UiState = "idle" | "starting" | "processing" | "success" | "error";

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const POLL_INTERVAL_MS = 2000;

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatFileSize(bytes: number) {
  if (bytes < 1024) return `${bytes} o`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
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

function StatBadge({ label, value, color }: { label: string; value: number; color: string }) {
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
      <span style={{ fontSize: 22, fontWeight: 700, color, fontVariantNumeric: "tabular-nums" }}>
        {value.toLocaleString()}
      </span>
      <span style={{ fontSize: 11, color: "#888", marginTop: 2 }}>{label}</span>
    </div>
  );
}

function FileResultCard({ result }: { result: DiplomeImportResult }) {
  const [expanded, setExpanded] = useState(false);
  const ignore = result.colonneAcademieAbsente > 0;
  const hasWarnings = (result.warnings?.length ?? 0) > 0;
  const hasIssues = ignore || result.nonTrouves > 0 || hasWarnings;

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
          background: ignore ? "#fffbeb" : hasIssues ? "#fef2f2" : "#f9fafb",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}>
          <span style={{ fontSize: 16 }}>{ignore ? "⏭️" : hasIssues ? "⚠️" : "✅"}</span>
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
          {ignore && (
            <p style={{ margin: "0 0 10px", fontSize: 12, color: "#92400e" }}>
              Fichier ignoré : colonne "N° Academie" absente (export à régénérer).
            </p>
          )}
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: hasWarnings ? 12 : 0 }}>
            <MiniStat label="Mis à jour" value={result.misAJour} color="#22c55e" />
            <MiniStat label="Introuvables" value={result.nonTrouves} color="#ef4444" />
          </div>

          {hasWarnings && (
            <div style={{ maxHeight: 160, overflowY: "auto", borderRadius: 8, border: "1px solid #f0f0f0", marginTop: 10 }}>
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

// ─── Component ────────────────────────────────────────────────────────────────

export default function ImportNumeroDiplomeModal({ open, onClose, onSuccess }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const [files, setFiles] = useState<File[]>([]);
  const [dragging, setDragging] = useState(false);
  const [state, setState] = useState<UiState>("idle");
  const [job, setJob] = useState<DiplomeImportJob | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const demarrerImportNumeroDiplomeAsync = useNouveauBachelierStore((s) => s.demarrerImportNumeroDiplomeAsync);
  const fetchJobImportNumeroDiplome = useNouveauBachelierStore((s) => s.fetchJobImportNumeroDiplome);

  const stopPolling = useCallback(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }, []);

  useEffect(() => () => stopPolling(), [stopPolling]);

  const isExcelFile = (f: File) => f.name.endsWith(".xlsx") || f.name.endsWith(".xls");

  const addFiles = (incoming: FileList | File[]) => {
    const valides = Array.from(incoming).filter(isExcelFile);
    setFiles((prev) => {
      const existants = new Set(prev.map((f) => `${f.name}_${f.size}`));
      const nouveaux = valides.filter((f) => !existants.has(`${f.name}_${f.size}`));
      return [...prev, ...nouveaux];
    });
  };

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    if (e.dataTransfer.files?.length) addFiles(e.dataTransfer.files);
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.length) addFiles(e.target.files);
  };

  const pollJob = (jobId: string) => {
    pollRef.current = setInterval(async () => {
      try {
        const current = await fetchJobImportNumeroDiplome(jobId);
        setJob(current);
        if (current.statut === "TERMINE") {
          stopPolling();
          setState("success");
          onSuccess?.();
        } else if (current.statut === "ECHEC") {
          stopPolling();
          setState("error");
          setErrorMsg(current.erreur || "Le traitement a échoué côté serveur.");
        }
      } catch (err: any) {
        stopPolling();
        setState("error");
        setErrorMsg(err?.response?.data?.message || err?.message || "Erreur lors du suivi du job");
      }
    }, POLL_INTERVAL_MS);
  };

  const handleUpload = async () => {
    if (files.length === 0) return;
    setState("starting");
    setJob(null);
    setErrorMsg(null);

    try {
      const jobId = await demarrerImportNumeroDiplomeAsync(files);
      setState("processing");
      pollJob(jobId);
    } catch (err: any) {
      setState("error");
      setErrorMsg(err?.response?.data?.message || err?.message || "Erreur serveur inconnue");
    }
  };

  const reset = () => {
    stopPolling();
    setFiles([]);
    setState("idle");
    setJob(null);
    setErrorMsg(null);
    if (inputRef.current) inputRef.current.value = "";
  };

  const totaux = (job?.resultats ?? []).reduce(
    (acc, r) => ({
      misAJour: acc.misAJour + r.misAJour,
      nonTrouves: acc.nonTrouves + r.nonTrouves,
      colonneAcademieAbsente: acc.colonneAcademieAbsente + (r.colonneAcademieAbsente > 0 ? 1 : 0),
      total: acc.total + r.total,
    }),
    { misAJour: 0, nonTrouves: 0, colonneAcademieAbsente: 0, total: 0 }
  );

  const totalSize = files.reduce((sum, f) => sum + f.size, 0);

  const dialogHeader = (
    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
      <div
        style={{
          width: 36, height: 36, borderRadius: 8,
          background: "#f5f3ff", border: "1px solid #ddd6fe",
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: 18,
        }}
      >
        🎓
      </div>
      <div>
        <p style={{ margin: 0, fontWeight: 600, fontSize: 15, color: "#111" }}>
          Mettre à jour le N° Diplôme
        </p>
        <p style={{ margin: 0, fontSize: 12, color: "#888" }}>
          Fichiers Excel avec colonne "N° Academie" — traitement en tâche de fond
        </p>
      </div>
    </div>
  );

  return (
    <Dialog
      visible={open}
      onHide={() => { reset(); onClose(); }}
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
            <div
              onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
              onDragLeave={() => setDragging(false)}
              onDrop={handleDrop}
              onClick={() => inputRef.current?.click()}
              style={{
                border: `2px dashed ${dragging || files.length > 0 ? "#7c3aed" : "#e5e7eb"}`,
                borderRadius: 12,
                padding: "24px 20px",
                textAlign: "center",
                cursor: "pointer",
                background: dragging || files.length > 0 ? "#f5f3ff" : "#fafafa",
                transition: "all .15s",
                marginBottom: 12,
              }}
            >
              <input
                ref={inputRef}
                type="file"
                accept=".xlsx,.xls"
                multiple
                style={{ display: "none" }}
                onChange={handleFileChange}
              />
              <div style={{ fontSize: 30, marginBottom: 6 }}>📁</div>
              <p style={{ margin: "0 0 4px", fontWeight: 500, color: "#374151", fontSize: 14 }}>
                Glissez vos fichiers Export-*.xlsx ici
              </p>
              <p style={{ margin: 0, fontSize: 12, color: "#9ca3af" }}>
                ou cliquez pour parcourir (plusieurs fichiers possibles)
              </p>
            </div>

            {files.length > 0 && (
              <div style={{ marginBottom: 16, border: "1px solid #e5e7eb", borderRadius: 10, maxHeight: 220, overflowY: "auto" }}>
                {files.map((f, i) => (
                  <div
                    key={`${f.name}_${f.size}_${i}`}
                    style={{
                      display: "flex", alignItems: "center", justifyContent: "space-between",
                      padding: "9px 12px",
                      borderBottom: i < files.length - 1 ? "1px solid #f0f0f0" : "none",
                    }}
                  >
                    <div style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}>
                      <span style={{ fontSize: 16 }}>📄</span>
                      <span
                        style={{
                          fontSize: 13, color: "#111", fontWeight: 500,
                          whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", maxWidth: 360,
                        }}
                        title={f.name}
                      >
                        {f.name}
                      </span>
                      <span style={{ fontSize: 11, color: "#9ca3af" }}>{formatFileSize(f.size)}</span>
                    </div>
                    <button
                      onClick={() => removeFile(i)}
                      style={{ background: "none", border: "none", color: "#ef4444", cursor: "pointer", fontSize: 14, padding: "2px 6px" }}
                      aria-label="Retirer"
                    >
                      ✕
                    </button>
                  </div>
                ))}
                <div style={{ padding: "8px 12px", fontSize: 12, color: "#6b7280", background: "#f9fafb" }}>
                  {files.length} fichier{files.length > 1 ? "s" : ""} — {formatFileSize(totalSize)} au total
                </div>
              </div>
            )}

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
              {files.length > 0 && (
                <button
                  onClick={reset}
                  style={{ padding: "9px 18px", borderRadius: 8, border: "1px solid #e5e7eb", background: "#fff", color: "#6b7280", fontSize: 13, cursor: "pointer", fontWeight: 500 }}
                >
                  Tout retirer
                </button>
              )}
              <button
                onClick={handleUpload}
                disabled={files.length === 0}
                style={{
                  padding: "9px 22px", borderRadius: 8, border: "none",
                  background: files.length > 0 ? "#7c3aed" : "#e5e7eb",
                  color: files.length > 0 ? "#fff" : "#9ca3af",
                  fontSize: 13, cursor: files.length > 0 ? "pointer" : "not-allowed", fontWeight: 600,
                  transition: "background .15s",
                }}
              >
                Lancer la mise à jour ({files.length})
              </button>
            </div>
          </>
        )}

        {/* ── STARTING / PROCESSING ── */}
        {(state === "starting" || state === "processing") && (
          <div style={{ textAlign: "center", padding: "8px 0 16px" }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#111", fontSize: 15 }}>
              {state === "starting" ? "Envoi des fichiers…" : "Traitement en tâche de fond…"}
            </p>
            <p style={{ margin: "0 0 4px", fontSize: 12, color: "#6b7280" }}>
              {files.length} fichier{files.length > 1 ? "s" : ""} — {formatFileSize(totalSize)}
            </p>
            {job && (
              <p style={{ margin: 0, fontSize: 12, color: "#7c3aed" }}>
                Statut : {job.statut === "EN_ATTENTE" ? "en attente" : "en cours"}…
              </p>
            )}
            <p style={{ margin: "10px 0 0", fontSize: 11, color: "#9ca3af" }}>
              Vous pouvez fermer cette fenêtre, le traitement continue côté serveur.
            </p>
          </div>
        )}

        {/* ── ERROR ── */}
        {state === "error" && (
          <div style={{ background: "#fef2f2", border: "1px solid #fecaca", borderRadius: 10, padding: "16px 20px", marginBottom: 16 }}>
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#dc2626", fontSize: 14 }}>
              ❌ Erreur lors de la mise à jour
            </p>
            <p style={{ margin: 0, fontSize: 13, color: "#6b7280" }}>{errorMsg}</p>
            <button
              onClick={reset}
              style={{ marginTop: 12, padding: "7px 16px", borderRadius: 7, border: "1px solid #fecaca", background: "#fff", color: "#dc2626", fontSize: 12, cursor: "pointer", fontWeight: 500 }}
            >
              Réessayer
            </button>
          </div>
        )}

        {/* ── SUCCESS + RESULTS ── */}
        {state === "success" && job?.resultats && (
          <>
            <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginBottom: 16, justifyContent: "center" }}>
              <StatBadge label="Mis à jour" value={totaux.misAJour} color="#16a34a" />
              <StatBadge label="Introuvables" value={totaux.nonTrouves} color="#ef4444" />
              <StatBadge label="Fichiers ignorés" value={totaux.colonneAcademieAbsente} color="#f59e0b" />
            </div>

            <p style={{ margin: "0 0 10px", fontSize: 12, color: "#6b7280", textAlign: "center" }}>
              {job.resultats.length} fichier{job.resultats.length > 1 ? "s" : ""} traité{job.resultats.length > 1 ? "s" : ""} — {totaux.total} ligne{totaux.total > 1 ? "s" : ""} au total
            </p>

            <div style={{ maxHeight: 320, overflowY: "auto", paddingRight: 2 }}>
              {job.resultats.map((r, i) => (
                <FileResultCard key={`${r.fichier}_${i}`} result={r} />
              ))}
            </div>

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", marginTop: 18 }}>
              <button
                onClick={reset}
                style={{ padding: "9px 18px", borderRadius: 8, border: "1px solid #e5e7eb", background: "#fff", color: "#374151", fontSize: 13, cursor: "pointer", fontWeight: 500 }}
              >
                Nouvel import
              </button>
              <button
                onClick={() => { reset(); onClose(); }}
                style={{ padding: "9px 22px", borderRadius: 8, border: "none", background: "#7c3aed", color: "#fff", fontSize: 13, cursor: "pointer", fontWeight: 600 }}
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

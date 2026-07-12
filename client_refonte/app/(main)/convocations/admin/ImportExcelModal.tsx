// components/ImportExcelModal.tsx
"use client";

import { useRef, useState, useCallback } from "react";
import { Dialog } from "primereact/dialog";
import axiosInstance from "@/app/api/axiosInstance";

// ─── Types ────────────────────────────────────────────────────────────────────

interface ImportError {
  row: number;
  message: string;
}

interface DuplicateRecord {
  numeroTable: string;
  jury: string;
}

interface IgnoredRecord {
  row: number;
  reason: string;
}

interface ImportResult {
  importedCount: number;
  errorRows: number;
  ignoredCount: number;
  errors: ImportError[];
  duplicates: DuplicateRecord[];
  ignored: IgnoredRecord[];
  missingEtablissementsPrincipaux: Record<string, number>;
  missingCentresEcrit: Record<string, number>;
  missingCentresEPS: Record<string, number>;
}

type TabKey = "errors" | "duplicates" | "ignored" | "missing";
type UploadState = "idle" | "uploading" | "success" | "error";

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

// ─── Component ────────────────────────────────────────────────────────────────

export default function ImportExcelModal({ open, onClose, onSuccess }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [dragging, setDragging] = useState(false);
  const [state, setState] = useState<UploadState>("idle");
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<ImportResult | null>(null);
  const [activeTab, setActiveTab] = useState<TabKey>("errors");
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // ── drag & drop ──
  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped && dropped.name.endsWith(".xlsx")) setFile(dropped);
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (f) setFile(f);
  };

  // ── upload ──
  const handleUpload = async () => {
    if (!file) return;
    setState("uploading");
    setProgress(0);
    setResult(null);
    setErrorMsg(null);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axiosInstance.post<ImportResult>(
        "candidats/import-excel",
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
          onUploadProgress: (evt) => {
            if (evt.total) {
              // Phase 1 : upload réseau → 0–40 %
              setProgress(Math.round((evt.loaded / evt.total) * 40));
            }
          },
        }
      );

      // Phase 2 : traitement serveur simulé en plusieurs étapes → 40–100 %
      for (let p = 40; p <= 95; p += 5) {
        await new Promise((r) => setTimeout(r, 80));
        setProgress(p);
      }
      setProgress(100);

      setResult(response.data);
      setState("success");
      onSuccess?.();
    } catch (err: any) {
      setState("error");
      setErrorMsg(
        err?.response?.data?.message || err?.message || "Erreur serveur inconnue"
      );
    }
  };

  const reset = () => {
    setFile(null);
    setState("idle");
    setProgress(0);
    setResult(null);
    setErrorMsg(null);
    if (inputRef.current) inputRef.current.value = "";
  };

  // ─── Missing keys helper ───
  const missingEntries = (map: Record<string, number>) =>
    Object.entries(map).sort((a, b) => b[1] - a[1]);

  // ─── Tab config ───
  const tabs: { key: TabKey; label: string; count: number; color: string }[] = [
    {
      key: "errors",
      label: "Erreurs",
      count: result?.errorRows ?? 0,
      color: "#ef4444",
    },
    {
      key: "duplicates",
      label: "Doublons",
      count: result?.duplicates.length ?? 0,
      color: "#f59e0b",
    },
    {
      key: "ignored",
      label: "Ignorés",
      count: result?.ignoredCount ?? 0,
      color: "#6366f1",
    },
    {
      key: "missing",
      label: "Établissements manquants",
      count:
        (result
          ? Object.keys(result.missingEtablissementsPrincipaux).length +
            Object.keys(result.missingCentresEcrit).length +
            Object.keys(result.missingCentresEPS).length
          : 0),
      color: "#0ea5e9",
    },
  ];

  const dialogHeader = (
    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
      <div
        style={{
          width: 36, height: 36, borderRadius: 8,
          background: "#f0fdf4", border: "1px solid #bbf7d0",
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: 18,
        }}
      >
        📊
      </div>
      <div>
        <p style={{ margin: 0, fontWeight: 600, fontSize: 15, color: "#111" }}>
          Importer depuis Excel
        </p>
        <p style={{ margin: 0, fontSize: 12, color: "#888" }}>
          Fichier .xlsx uniquement
        </p>
      </div>
    </div>
  );

  return (
    <Dialog
      visible={open}
      onHide={onClose}
      header={dialogHeader}
      modal
      style={{ width: "640px" }}
      contentStyle={{ padding: "20px 24px 24px" }}
      draggable={false}
      resizable={false}
    >
        <div>
          {/* ── IDLE / FILE SELECTED ── */}
          {state === "idle" && (
            <>
              {/* Drop zone */}
              <div
                onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
                onDragLeave={() => setDragging(false)}
                onDrop={handleDrop}
                onClick={() => inputRef.current?.click()}
                style={{
                  border: `2px dashed ${dragging ? "#22c55e" : file ? "#22c55e" : "#e5e7eb"}`,
                  borderRadius: 12,
                  padding: "28px 20px",
                  textAlign: "center",
                  cursor: "pointer",
                  background: dragging
                    ? "#f0fdf4"
                    : file
                    ? "#f0fdf4"
                    : "#fafafa",
                  transition: "all .15s",
                  marginBottom: 16,
                }}
              >
                <input
                  ref={inputRef}
                  type="file"
                  accept=".xlsx"
                  style={{ display: "none" }}
                  onChange={handleFileChange}
                />
                {file ? (
                  <>
                    <div style={{ fontSize: 32, marginBottom: 6 }}>📄</div>
                    <p
                      style={{
                        margin: "0 0 2px",
                        fontWeight: 600,
                        color: "#16a34a",
                        fontSize: 14,
                      }}
                    >
                      {file.name}
                    </p>
                    <p style={{ margin: 0, fontSize: 12, color: "#6b7280" }}>
                      {formatFileSize(file.size)}
                    </p>
                  </>
                ) : (
                  <>
                    <div style={{ fontSize: 32, marginBottom: 8 }}>📁</div>
                    <p
                      style={{
                        margin: "0 0 4px",
                        fontWeight: 500,
                        color: "#374151",
                        fontSize: 14,
                      }}
                    >
                      Glissez votre fichier ici
                    </p>
                    <p style={{ margin: 0, fontSize: 12, color: "#9ca3af" }}>
                      ou cliquez pour parcourir
                    </p>
                  </>
                )}
              </div>

              {/* Actions */}
              <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
                {file && (
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
                    Changer
                  </button>
                )}
                <button
                  onClick={handleUpload}
                  disabled={!file}
                  style={{
                    padding: "9px 22px",
                    borderRadius: 8,
                    border: "none",
                    background: file ? "#16a34a" : "#e5e7eb",
                    color: file ? "#fff" : "#9ca3af",
                    fontSize: 13,
                    cursor: file ? "pointer" : "not-allowed",
                    fontWeight: 600,
                    transition: "background .15s",
                  }}
                >
                  Lancer l'import
                </button>
              </div>
            </>
          )}

          {/* ── UPLOADING ── */}
          {state === "uploading" && (
            <div style={{ textAlign: "center", padding: "8px 0 16px" }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
              <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#111", fontSize: 15 }}>
                Import en cours…
              </p>
              <p style={{ margin: "0 0 20px", fontSize: 12, color: "#6b7280" }}>
                {file?.name} — {formatFileSize(file?.size ?? 0)}
              </p>

              {/* Progress bar */}
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
                    ? "📡 Envoi du fichier"
                    : progress < 100
                    ? "⚙️ Traitement serveur"
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
          {state === "success" && result && (
            <>
              {/* Stats */}
              <div
                style={{
                  display: "flex",
                  gap: 10,
                  flexWrap: "wrap",
                  marginBottom: 20,
                  justifyContent: "center",
                }}
              >
                <StatBadge label="Importés" value={result.importedCount} color="#16a34a" />
                <StatBadge label="Erreurs" value={result.errorRows} color="#ef4444" />
                <StatBadge label="Doublons" value={result.duplicates.length} color="#f59e0b" />
                <StatBadge label="Ignorés" value={result.ignoredCount} color="#6366f1" />
              </div>

              {/* Tabs */}
              {(result.errorRows > 0 ||
                result.duplicates.length > 0 ||
                result.ignoredCount > 0 ||
                Object.keys(result.missingEtablissementsPrincipaux).length > 0) && (
                <>
                  {/* Tab bar */}
                  <div
                    style={{
                      display: "flex",
                      gap: 4,
                      borderBottom: "1px solid #e5e7eb",
                      marginBottom: 14,
                      overflowX: "auto",
                    }}
                  >
                    {tabs
                      .filter((t) => t.count > 0)
                      .map((t) => (
                        <button
                          key={t.key}
                          onClick={() => setActiveTab(t.key)}
                          style={{
                            padding: "8px 14px",
                            border: "none",
                            background: "none",
                            cursor: "pointer",
                            fontSize: 12,
                            fontWeight: activeTab === t.key ? 600 : 400,
                            color: activeTab === t.key ? t.color : "#6b7280",
                            borderBottom:
                              activeTab === t.key
                                ? `2px solid ${t.color}`
                                : "2px solid transparent",
                            whiteSpace: "nowrap",
                            display: "flex",
                            alignItems: "center",
                            gap: 6,
                          }}
                        >
                          {t.label}
                          <span
                            style={{
                              background: t.color + "20",
                              color: t.color,
                              borderRadius: 99,
                              padding: "1px 7px",
                              fontSize: 11,
                              fontWeight: 600,
                            }}
                          >
                            {t.count}
                          </span>
                        </button>
                      ))}
                  </div>

                  {/* Tab content */}
                  <div
                    style={{
                      maxHeight: 240,
                      overflowY: "auto",
                      borderRadius: 8,
                      border: "1px solid #f0f0f0",
                    }}
                  >
                    {/* Errors */}
                    {activeTab === "errors" && result.errors.length > 0 && (
                      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
                        <thead>
                          <tr style={{ background: "#fef2f2", position: "sticky", top: 0 }}>
                            <th style={{ padding: "8px 12px", textAlign: "left", color: "#ef4444", fontWeight: 600, width: 60 }}>Ligne</th>
                            <th style={{ padding: "8px 12px", textAlign: "left", color: "#ef4444", fontWeight: 600 }}>Message</th>
                          </tr>
                        </thead>
                        <tbody>
                          {result.errors.map((e, i) => (
                            <tr key={i} style={{ borderTop: "1px solid #fef2f2" }}>
                              <td style={{ padding: "7px 12px", color: "#6b7280", fontFamily: "monospace" }}>{e.row}</td>
                              <td style={{ padding: "7px 12px", color: "#374151" }}>{e.message}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}

                    {/* Duplicates */}
                    {activeTab === "duplicates" && result.duplicates.length > 0 && (
                      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
                        <thead>
                          <tr style={{ background: "#fffbeb", position: "sticky", top: 0 }}>
                            <th style={{ padding: "8px 12px", textAlign: "left", color: "#d97706", fontWeight: 600 }}>N° Table</th>
                            <th style={{ padding: "8px 12px", textAlign: "left", color: "#d97706", fontWeight: 600 }}>Jury</th>
                          </tr>
                        </thead>
                        <tbody>
                          {result.duplicates.map((d, i) => (
                            <tr key={i} style={{ borderTop: "1px solid #fef3c7" }}>
                              <td style={{ padding: "7px 12px", color: "#374151", fontFamily: "monospace" }}>{d.numeroTable}</td>
                              <td style={{ padding: "7px 12px", color: "#6b7280" }}>{d.jury}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}

                    {/* Ignored */}
                    {activeTab === "ignored" && result.ignored.length > 0 && (
                      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
                        <thead>
                          <tr style={{ background: "#eef2ff", position: "sticky", top: 0 }}>
                            <th style={{ padding: "8px 12px", textAlign: "left", color: "#6366f1", fontWeight: 600, width: 60 }}>Ligne</th>
                            <th style={{ padding: "8px 12px", textAlign: "left", color: "#6366f1", fontWeight: 600 }}>Raison</th>
                          </tr>
                        </thead>
                        <tbody>
                          {result.ignored.map((ig, i) => (
                            <tr key={i} style={{ borderTop: "1px solid #eef2ff" }}>
                              <td style={{ padding: "7px 12px", color: "#6b7280", fontFamily: "monospace" }}>{ig.row}</td>
                              <td style={{ padding: "7px 12px", color: "#374151" }}>{ig.reason}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}

                    {/* Missing */}
                    {activeTab === "missing" && (
                      <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 12 }}>
                        {missingEntries(result.missingEtablissementsPrincipaux).length > 0 && (
                          <div>
                            <p style={{ margin: "0 0 6px", fontSize: 11, fontWeight: 600, color: "#0ea5e9", textTransform: "uppercase", letterSpacing: ".5px" }}>Établissements principaux</p>
                            {missingEntries(result.missingEtablissementsPrincipaux).map(([k, v]) => (
                              <div key={k} style={{ display: "flex", justifyContent: "space-between", fontSize: 12, padding: "3px 0", borderBottom: "1px solid #f0f9ff" }}>
                                <span style={{ color: "#374151" }}>{k}</span>
                                <span style={{ color: "#0ea5e9", fontWeight: 600 }}>{v}×</span>
                              </div>
                            ))}
                          </div>
                        )}
                        {missingEntries(result.missingCentresEcrit).length > 0 && (
                          <div>
                            <p style={{ margin: "0 0 6px", fontSize: 11, fontWeight: 600, color: "#0ea5e9", textTransform: "uppercase", letterSpacing: ".5px" }}>Centres d'écrit</p>
                            {missingEntries(result.missingCentresEcrit).map(([k, v]) => (
                              <div key={k} style={{ display: "flex", justifyContent: "space-between", fontSize: 12, padding: "3px 0", borderBottom: "1px solid #f0f9ff" }}>
                                <span style={{ color: "#374151" }}>{k}</span>
                                <span style={{ color: "#0ea5e9", fontWeight: 600 }}>{v}×</span>
                              </div>
                            ))}
                          </div>
                        )}
                        {missingEntries(result.missingCentresEPS).length > 0 && (
                          <div>
                            <p style={{ margin: "0 0 6px", fontSize: 11, fontWeight: 600, color: "#0ea5e9", textTransform: "uppercase", letterSpacing: ".5px" }}>Centres EPS</p>
                            {missingEntries(result.missingCentresEPS).map(([k, v]) => (
                              <div key={k} style={{ display: "flex", justifyContent: "space-between", fontSize: 12, padding: "3px 0", borderBottom: "1px solid #f0f9ff" }}>
                                <span style={{ color: "#374151" }}>{k}</span>
                                <span style={{ color: "#0ea5e9", fontWeight: 600 }}>{v}×</span>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </>
              )}

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
                  onClick={onClose}
                  style={{
                    padding: "9px 22px",
                    borderRadius: 8,
                    border: "none",
                    background: "#16a34a",
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
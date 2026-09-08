"use client";

import { useRef, useState, useCallback } from "react";
import { Dialog } from "primereact/dialog";
import { ImportLog, useBacheliersToCampusenStore } from "../bacheliersToCampusenStore";

// ─── Types ────────────────────────────────────────────────────────────────────

type UploadState = "idle" | "uploading" | "success" | "error";

interface Props {
  open: boolean;
  onClose: () => void;
  defaultAnnee?: number | null;
  onSuccess?: (log: ImportLog) => void;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatFileSize(bytes: number) {
  if (bytes < 1024) return `${bytes} o`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
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

// ─── Component ────────────────────────────────────────────────────────────────

export default function ImportBacheliersModal({ open, onClose, defaultAnnee, onSuccess }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [dragging, setDragging] = useState(false);
  const [state, setState] = useState<UploadState>("idle");
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<ImportLog | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [annee, setAnnee] = useState<number>(defaultAnnee ?? new Date().getFullYear());
  const [remplacer, setRemplacer] = useState(false);

  const importer = useBacheliersToCampusenStore((s) => s.importer);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped && dropped.name.toLowerCase().endsWith(".xlsx")) {
      setFile(dropped);
    }
  }, []);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (f) setFile(f);
  };

  const handleUpload = async () => {
    if (!file || !annee) return;
    setState("uploading");
    setProgress(0);
    setResult(null);
    setErrorMsg(null);

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
      const data = await importer(file, annee, remplacer);
      clearInterval(interval);
      setProgress(100);
      setResult(data);
      setState("success");
      onSuccess?.(data);
    } catch (err: any) {
      clearInterval(interval);
      setState("error");
      setErrorMsg(err?.response?.data?.message || err?.message || "Erreur serveur inconnue");
    }
  };

  const reset = () => {
    setFile(null);
    setState("idle");
    setProgress(0);
    setResult(null);
    setErrorMsg(null);
    setRemplacer(false);
    if (inputRef.current) inputRef.current.value = "";
  };

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
          Importer les bacheliers Campusen
        </p>
        <p style={{ margin: 0, fontSize: 12, color: "#888" }}>
          Fichier .xlsx (une feuille par série)
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
      style={{ width: "640px" }}
      contentStyle={{ padding: "20px 24px 24px" }}
      draggable={false}
      resizable={false}
    >
      <div>
        {state === "idle" && (
          <>
            {/* Année + option remplacer */}
            <div style={{ display: "flex", gap: 16, marginBottom: 16, alignItems: "flex-end", flexWrap: "wrap" }}>
              <div>
                <label style={{ display: "block", fontSize: 12, fontWeight: 600, color: "#374151", marginBottom: 6 }}>
                  Session (année) *
                </label>
                <input
                  type="number"
                  value={annee}
                  onChange={(e) => setAnnee(Number(e.target.value))}
                  style={{
                    padding: "8px 12px", borderRadius: 8, border: "1px solid #e5e7eb",
                    fontSize: 13, width: 140,
                  }}
                />
              </div>
              <label style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13, color: "#374151", paddingBottom: 8 }}>
                <input type="checkbox" checked={remplacer} onChange={(e) => setRemplacer(e.target.checked)} />
                Remplacer les données existantes de cette session
              </label>
            </div>

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
                background: dragging || file ? "#f0fdf4" : "#fafafa",
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
                  <p style={{ margin: "0 0 2px", fontWeight: 600, color: "#16a34a", fontSize: 14 }}>
                    {file.name}
                  </p>
                  <p style={{ margin: 0, fontSize: 12, color: "#6b7280" }}>
                    {formatFileSize(file.size)}
                  </p>
                </>
              ) : (
                <>
                  <div style={{ fontSize: 32, marginBottom: 8 }}>📁</div>
                  <p style={{ margin: "0 0 4px", fontWeight: 500, color: "#374151", fontSize: 14 }}>
                    Glissez votre fichier ici
                  </p>
                  <p style={{ margin: 0, fontSize: 12, color: "#9ca3af" }}>
                    ou cliquez pour parcourir
                  </p>
                </>
              )}
            </div>

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
              {file && (
                <button
                  onClick={reset}
                  style={{
                    padding: "9px 18px", borderRadius: 8, border: "1px solid #e5e7eb",
                    background: "#fff", color: "#6b7280", fontSize: 13, cursor: "pointer", fontWeight: 500,
                  }}
                >
                  Changer
                </button>
              )}
              <button
                onClick={handleUpload}
                disabled={!file || !annee}
                style={{
                  padding: "9px 22px", borderRadius: 8, border: "none",
                  background: file && annee ? "#16a34a" : "#e5e7eb",
                  color: file && annee ? "#fff" : "#9ca3af",
                  fontSize: 13, cursor: file && annee ? "pointer" : "not-allowed",
                  fontWeight: 600, transition: "background .15s",
                }}
              >
                Lancer l'import
              </button>
            </div>
          </>
        )}

        {state === "uploading" && (
          <div style={{ textAlign: "center", padding: "8px 0 16px" }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>⏳</div>
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#111", fontSize: 15 }}>
              Import en cours…
            </p>
            <p style={{ margin: "0 0 20px", fontSize: 12, color: "#6b7280" }}>
              {file?.name} — {formatFileSize(file?.size ?? 0)}
            </p>
            <div style={{ height: 8, borderRadius: 99, background: "#e5e7eb", overflow: "hidden", marginBottom: 8 }}>
              <div
                style={{
                  height: "100%", width: `${progress}%`,
                  background: progress < 40 ? "#3b82f6" : progress < 100 ? "#8b5cf6" : "#22c55e",
                  borderRadius: 99, transition: "width .3s ease, background .5s",
                }}
              />
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", fontSize: 11, color: "#9ca3af" }}>
              <span>
                {progress < 40 ? "📡 Envoi du fichier" : progress < 100 ? "⚙️ Traitement serveur" : "✅ Finalisation"}
              </span>
              <span>{progress}%</span>
            </div>
          </div>
        )}

        {state === "error" && (
          <div style={{ background: "#fef2f2", border: "1px solid #fecaca", borderRadius: 10, padding: "16px 20px", marginBottom: 16 }}>
            <p style={{ margin: "0 0 6px", fontWeight: 600, color: "#dc2626", fontSize: 14 }}>
              ❌ Erreur lors de l'import
            </p>
            <p style={{ margin: 0, fontSize: 13, color: "#6b7280" }}>{errorMsg}</p>
            <button
              onClick={reset}
              style={{
                marginTop: 12, padding: "7px 16px", borderRadius: 7, border: "1px solid #fecaca",
                background: "#fff", color: "#dc2626", fontSize: 12, cursor: "pointer", fontWeight: 500,
              }}
            >
              Réessayer
            </button>
          </div>
        )}

        {state === "success" && result && (
          <>
            <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginBottom: 20, justifyContent: "center" }}>
              <StatBadge label="Candidats" value={result.nombreCandidats} color="#16a34a" />
              <StatBadge label="Nouveaux" value={result.nombreNouveaux} color="#22c55e" />
              <StatBadge label="Mis à jour" value={result.nombreMisAJour} color="#f59e0b" />
              <StatBadge label="Feuilles" value={result.nombreFeuilles} color="#2563eb" />
            </div>

            {result.candidatsParSerie && Object.keys(result.candidatsParSerie).length > 0 && (
              <div style={{ borderRadius: 8, border: "1px solid #f0f0f0", maxHeight: 220, overflowY: "auto" }}>
                <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}>
                  <thead>
                    <tr style={{ background: "#f0fdf4", position: "sticky", top: 0 }}>
                      <th style={{ padding: "8px 12px", textAlign: "left", color: "#16a34a", fontWeight: 600 }}>Série</th>
                      <th style={{ padding: "8px 12px", textAlign: "right", color: "#16a34a", fontWeight: 600 }}>Total</th>
                      <th style={{ padding: "8px 12px", textAlign: "right", color: "#22c55e", fontWeight: 600 }}>Nouveaux</th>
                      <th style={{ padding: "8px 12px", textAlign: "right", color: "#f59e0b", fontWeight: 600 }}>Mis à jour</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Object.entries(result.candidatsParSerie).map(([serie, stat]) => (
                      <tr key={serie} style={{ borderTop: "1px solid #f0fdf4" }}>
                        <td style={{ padding: "7px 12px", color: "#374151" }}>{serie}</td>
                        <td style={{ padding: "7px 12px", color: "#111", textAlign: "right", fontFamily: "monospace" }}>{stat.total}</td>
                        <td style={{ padding: "7px 12px", color: "#16a34a", textAlign: "right", fontFamily: "monospace" }}>{stat.nouveaux}</td>
                        <td style={{ padding: "7px 12px", color: "#d97706", textAlign: "right", fontFamily: "monospace" }}>{stat.misAJour}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", marginTop: 18 }}>
              <button
                onClick={reset}
                style={{
                  padding: "9px 18px", borderRadius: 8, border: "1px solid #e5e7eb",
                  background: "#fff", color: "#374151", fontSize: 13, cursor: "pointer", fontWeight: 500,
                }}
              >
                Nouvel import
              </button>
              <button
                onClick={() => { reset(); onClose(); }}
                style={{
                  padding: "9px 22px", borderRadius: 8, border: "none",
                  background: "#16a34a", color: "#fff", fontSize: 13, cursor: "pointer", fontWeight: 600,
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

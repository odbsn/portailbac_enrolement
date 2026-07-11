import JSZip from 'jszip';

/**
 * Résultat de l'extraction d'un zip.
 */
export interface ZipExtractionResult {
  /** Le zip d'origine, pour affichage/erreurs */
  sourceZipName: string;
  /** Le .xlsx extrait, prêt à être envoyé au serveur comme un fichier normal */
  file: File | null;
  /** Présent si l'extraction a échoué ou si aucun .xlsx n'a été trouvé */
  error: string | null;
}

/**
 * Cherche le premier fichier se terminant par .xlsx ou .xls dans le zip,
 * à n'importe quelle profondeur (dossiers imbriqués type "OBUCD_BAC202607/...").
 * Ignore les dossiers et les fichiers macOS __MACOSX.
 */
export async function extractXlsxFromZip(zipFile: File): Promise<ZipExtractionResult> {
  try {
    const zip = await JSZip.loadAsync(zipFile);

    const candidat = Object.values(zip.files).find((entry) => {
      if (entry.dir) return false;
      if (entry.name.includes('__MACOSX')) return false;
      const lower = entry.name.toLowerCase();
      return lower.endsWith('.xlsx') || lower.endsWith('.xls');
    });

    if (!candidat) {
      return {
        sourceZipName: zipFile.name,
        file: null,
        error: `Aucun fichier .xlsx trouvé dans ${zipFile.name}`,
      };
    }

    const blob = await candidat.async('blob');

    // Nom du fichier extrait : on garde juste le nom (sans le chemin des sous-dossiers),
    // préfixé par le nom du zip pour rester traçable côté résultats d'import.
    const nomFichierExtrait = candidat.name.split('/').pop() || candidat.name;
    const nomFinal = nomFichierExtrait.toLowerCase().endsWith('.xlsx') || nomFichierExtrait.toLowerCase().endsWith('.xls')
      ? nomFichierExtrait
      : `${nomFichierExtrait}.xlsx`;

    const extractedFile = new File([blob], nomFinal, {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    return {
      sourceZipName: zipFile.name,
      file: extractedFile,
      error: null,
    };
  } catch (e: any) {
    return {
      sourceZipName: zipFile.name,
      file: null,
      error: `Impossible de lire ${zipFile.name} : ${e?.message || 'fichier zip invalide'}`,
    };
  }
}

/**
 * Extrait le .xlsx de chaque zip fourni, en parallèle.
 * Retourne la liste des fichiers extraits avec succès, et la liste des erreurs séparément.
 */
export async function extractXlsxFromZips(zipFiles: File[]): Promise<{
  files: File[];
  errors: string[];
}> {
  const results = await Promise.all(zipFiles.map(extractXlsxFromZip));

  const files: File[] = [];
  const errors: string[] = [];

  for (const r of results) {
    if (r.file) {
      files.push(r.file);
    } else if (r.error) {
      errors.push(r.error);
    }
  }

  return { files, errors };
}
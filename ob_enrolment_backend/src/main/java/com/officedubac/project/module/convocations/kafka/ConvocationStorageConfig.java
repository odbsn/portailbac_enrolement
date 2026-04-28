package com.officedubac.project.module.convocations.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class ConvocationStorageConfig {

    @Value("${convocation.storage.path:/opt/ob-data/convocations}")
    private String storagePath;

    @Getter
    private Path basePath;

    @PostConstruct
    public void init() {
        // Résoudre le chemin absolu
        this.basePath = Paths.get(storagePath).toAbsolutePath();

        log.info("📁 Initialisation du dossier de convocations...");
        log.info("   Chemin configuré: {}", storagePath);
        log.info("   Chemin absolu: {}", basePath);

        try {
            // Créer le dossier s'il n'existe pas
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("✅ Dossier de convocations créé avec succès: {}", basePath);
            } else {
                log.info("✅ Dossier de convocations déjà existant: {}", basePath);
            }

            // Vérifier les droits d'écriture
            checkWritePermission();

            // Afficher la taille du dossier
            logDirectorySize();

            log.info("✅ Configuration du stockage des convocations terminée");
            log.info("   Profil actif: {}", System.getProperty("spring.profiles.active", "default"));

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'initialisation du dossier de convocations: {}", e.getMessage(), e);
            // Option: lancer une exception pour empêcher le démarrage
            // throw new RuntimeException("Impossible d'initialiser le dossier de convocations", e);
        }
    }

    /**
     * Vérifie que l'application a les droits d'écriture sur le dossier
     */
    private void checkWritePermission() {
        try {
            Path testFile = basePath.resolve(".write_test_" + System.currentTimeMillis());
            Files.write(testFile, "test".getBytes());
            Files.delete(testFile);
            log.info("   ✅ Droits d'écriture vérifiés");
        } catch (IOException e) {
            log.error("   ❌ Pas de droits d'écriture sur le dossier: {}", basePath);
            log.error("   Solution: exécutez 'sudo chown -R $USER:$USER {}'", basePath);
        }
    }

    /**
     * Affiche la taille du dossier et le nombre de fichiers
     */
    private void logDirectorySize() {
        try {
            long fileCount = Files.walk(basePath)
                    .filter(Files::isRegularFile)
                    .count();

            long totalSize = Files.walk(basePath)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();

            log.info("   📊 Dossier contient: {} fichier(s) - Taille totale: {} MB",
                    fileCount, totalSize / (1024 * 1024));
        } catch (IOException e) {
            log.warn("   ⚠️ Impossible de calculer la taille du dossier: {}", e.getMessage());
        }
    }

    /**
     * Retourne le chemin pour un centre spécifique
     */
    public Path getCentrePath(String centreCode) {
        return basePath.resolve(sanitizeCode(centreCode));
    }

    /**
     * Retourne le chemin pour une convocation spécifique
     */
    public Path getConvocationPath(String centreCode, String numeroTable) {
        return getCentrePath(centreCode).resolve(numeroTable + ".pdf");
    }

    /**
     * Nettoie le code pour l'utiliser comme nom de dossier
     */
    private String sanitizeCode(String code) {
        if (code == null) return "unknown";
        return code.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
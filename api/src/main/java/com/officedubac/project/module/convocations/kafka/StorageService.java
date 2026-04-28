package com.officedubac.project.module.convocations.kafka;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class StorageService {

    private final Path root = Paths.get("convocations");

    public String save(byte[] pdf, String numeroTable) throws IOException {

        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        String fileName = numeroTable + ".pdf";
        Path filePath = root.resolve(fileName);

        Files.write(filePath, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return "/files/convocations/" + fileName;
    }
}

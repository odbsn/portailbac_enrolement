package com.officedubac.project.module.convocations.kafka;

import com.officedubac.project.module.candidatFinis.CandidatFinisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/convocationMassive")
@RequiredArgsConstructor
public class ConvocationController {

    private final KafkaTemplate<String, ConvocationJob> kafkaTemplate;
    private final CandidatFinisRepository candidatRepository;

    // ===============================
    // 🔹 1. Générer UNE convocation
    // ===============================
    @PostMapping("/generate/{numeroTable}")
    public ResponseEntity<String> generateOne(@PathVariable String numeroTable) {

        kafkaTemplate.send("convocation.request",
                numeroTable,
                new ConvocationJob(numeroTable, null));

        return ResponseEntity.ok("Convocation envoyée en génération pour : " + numeroTable);
    }

    // ===============================
    // 🔹 2. Générer PAR SERIE
    // ===============================
    @PostMapping("/generate/serie/{serie}")
    public ResponseEntity<String> generateBySerie(@PathVariable String serie) {

        List<String> numeros = candidatRepository.findNumeroTableBySerie(serie)
                .stream()
                .map(NumeroTableOnly::getNumeroTable)
                .toList();

        numeros.forEach(num -> {
            kafkaTemplate.send("convocation.request",
                    num,
                    new ConvocationJob(num, serie));
        });

        return ResponseEntity.ok("Génération lancée pour la série : " + serie +
                " (" + numeros.size() + " candidats)");
    }

    // ===============================
    // 🔹 3. Générer TOUT
    // ===============================
    @PostMapping("/generate/all")
    public ResponseEntity<String> generateAll() {

        candidatRepository.streamAllNumeroTable().forEach(num -> {
            kafkaTemplate.send("convocation.request",
                    num,
                    new ConvocationJob(num, null));
        });

        return ResponseEntity.ok("Génération de toutes les convocations lancée 🚀");
    }
}

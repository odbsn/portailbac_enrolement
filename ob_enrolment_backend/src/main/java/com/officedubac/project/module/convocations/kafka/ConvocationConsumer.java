package com.officedubac.project.module.convocations.kafka;

import com.officedubac.project.module.convocations.ConvocationPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvocationConsumer {

    private final ConvocationPdfService pdfService;
    private final StorageService storageService;

    @KafkaListener(topics = "convocation.request", groupId = "pdf-workers")
    public void consume(ConvocationJob job) {

        try {
            byte[] pdf = pdfService.generateConvocation(job.getNumeroTable()).block();

            String url = storageService.save(pdf, job.getNumeroTable());

            // envoyer event success
            // kafkaTemplate.send("convocation.generated", ...)

        } catch (Exception e) {
            // kafkaTemplate.send("convocation.error", ...)
        }
    }
}
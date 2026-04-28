package com.officedubac.project.module.convocations.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvocationProducer {

    private final KafkaTemplate<String, ConvocationJob> kafkaTemplate;

    public void sendJob(ConvocationJob job) {
        kafkaTemplate.send("convocation.request", job.getNumeroTable(), job);
    }
}

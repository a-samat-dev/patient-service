package kz.smarthealth.patientservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.smarthealth.patientservice.aop.Log;
import kz.smarthealth.patientservice.model.entity.PatientDocument;
import kz.smarthealth.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PatientKafkaConsumerService {

    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper;

    @Log
    @KafkaListener(topics = "user-service-new-patients", groupId = "group-1")
    public void consume(String message) throws JsonProcessingException {
        PatientDocument patientDocument = objectMapper.readValue(message, PatientDocument.class);
        patientDocument.setCreatedAt(OffsetDateTime.now());
        patientRepository.save(patientDocument);
    }
}

package kz.smarthealth.patientservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.smarthealth.patientservice.model.entity.PatientDocument;
import kz.smarthealth.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PatientKafkaConsumerService}
 *
 * Created by Samat Abibulla on 2022-06-09
 */
@ExtendWith(MockitoExtension.class)
class PatientKafkaConsumerServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private PatientKafkaConsumerService underTest;

    @Test
    void consume_savesPatient() throws JsonProcessingException {
        // given
        String message = "{\"userId\":\"319bf132-56cd-480b-863c-9794ab375a00\",\"firstName\":\"Samat10\",\"birthDate\":\"2000-01-01\",\"phoneNumber\":\"12345678\"}";
        ArgumentCaptor<PatientDocument> patientDocumentArgumentCaptor = ArgumentCaptor.forClass(PatientDocument.class);
        // when
        underTest.consume(message);
        // then
        verify(patientRepository).save(patientDocumentArgumentCaptor.capture());
        PatientDocument patientDocument = patientDocumentArgumentCaptor.getValue();

        assertNotNull(patientDocument);
        assertEquals("319bf132-56cd-480b-863c-9794ab375a00", patientDocument.getUserId());
        assertEquals("Samat10", patientDocument.getFirstName());
        assertEquals(LocalDate.of(2000, 1, 1), patientDocument.getBirthDate());
        assertEquals("12345678", patientDocument.getPhoneNumber());
        assertNotNull(patientDocument.getCreatedAt());
    }
}
package kz.smarthealth.patientservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.patientservice.model.dto.ErrorResponseDTO;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.dto.UserRole;
import kz.smarthealth.patientservice.model.entity.PatientDocument;
import kz.smarthealth.patientservice.repository.PatientRepository;
import kz.smarthealth.patientservice.util.AppConstants;
import kz.smarthealth.patientservice.util.MessageSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static kz.smarthealth.patientservice.util.TestData.getPatientDTO;
import static kz.smarthealth.patientservice.util.TestData.getPatientEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link PatientController}
 *
 * Created by Samat Abibulla on 2023-03-27
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class PatientControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @AfterEach
    void afterEach() {
        patientRepository.deleteAll();
    }

    @Test
    void savePatient_returnsUnauthorized_whenUserUnauthorized() throws Exception {
        // given
        PatientDTO patientDTO = getPatientDTO();
        patientDTO.setId(null);
        patientDTO.setCreatedAt(null);
        String requestBody = objectMapper.writeValueAsString(patientDTO);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void savePatient_returnsForbidden_whenOrganizationRole() throws Exception {
        // given
        UUID organizationId = UUID.randomUUID();
        PatientDTO patientDTO = getPatientDTO();
        patientDTO.setId(null);
        patientDTO.setCreatedAt(null);
        String requestBody = objectMapper.writeValueAsString(patientDTO);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("userId", organizationId)
                        .header("role", UserRole.ROLE_ORGANIZATION)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void savePatient_returnsBadRequest_whenInvalidFieldsProvided() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        PatientDTO patientDTO = getPatientDTO();
        patientDTO.setId(null);
        patientDTO.setUserId(null);
        patientDTO.setFirstName(null);
        patientDTO.setBirthDate(null);
        patientDTO.setCreatedAt(null);
        String requestBody = objectMapper.writeValueAsString(patientDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("userId", userId)
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);
        Map<String, String> invalidFields = errorResponseDTO.getInvalidFields();

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals("Validation Error", errorResponseDTO.getMessage());
        assertEquals(3, invalidFields.size());

    }

    @Test
    void savePatient_savesPatient_underPatientRole() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        PatientDTO patientDTO = getPatientDTO();
        patientDTO.setId(null);
        patientDTO.setCreatedAt(null);
        String requestBody = objectMapper.writeValueAsString(patientDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("userId", userId)
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        validateSuccessfulSaveResult(patientDTO, mvcResult);
    }

    @Test
    void savePatient_savesPatient_underDoctorRole() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        PatientDTO patientDTO = getPatientDTO();
        patientDTO.setId(null);
        patientDTO.setCreatedAt(null);
        String requestBody = objectMapper.writeValueAsString(patientDTO);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header("userId", userId)
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isCreated()).andReturn();
        // then
        validateSuccessfulSaveResult(patientDTO, mvcResult);
    }

    private void validateSuccessfulSaveResult(PatientDTO patientDTO, MvcResult mvcResult)
            throws JsonProcessingException, UnsupportedEncodingException {
        PatientDTO createdPatientDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                PatientDTO.class);
        Map<String, Object> systemValues = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<HashMap<String, Object>>() {
                });

        assertNotNull(createdPatientDTO);
        assertNotNull(systemValues.get("id"));
        assertNotNull(systemValues.get("createdAt"));

        assertEquals(patientDTO.getUserId(), createdPatientDTO.getUserId());
        assertEquals(patientDTO.getFirstName(), createdPatientDTO.getFirstName());
        assertEquals(patientDTO.getLastName(), createdPatientDTO.getLastName());
        assertEquals(patientDTO.getBirthDate(), createdPatientDTO.getBirthDate());
        assertEquals(patientDTO.getPhoneNumber(), createdPatientDTO.getPhoneNumber());
        assertEquals(patientDTO.getFamilyConnectionId(), createdPatientDTO.getFamilyConnectionId());
        assertEquals(patientDTO.getIin(), createdPatientDTO.getIin());
    }

    @Test
    void getPatientById_returnsUnauthorized_whenUserUnauthorized() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/" + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void getPatientById_returnsForbidden_whenUserIsNotOwner() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/" + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void getPatientById_returnsPatient_whenUserIsOwner() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/"
                                + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientDocument.getUserId())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetResult(patientDocument, mvcResult);
    }

    @Test
    void getPatientById_returnsPatient_underDoctorRole() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/"
                                + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetResult(patientDocument, mvcResult);
    }

    @Test
    void getPatientById_returnsPatient_underOrganizationRole() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/"
                                + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_ORGANIZATION)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetResult(patientDocument, mvcResult);
    }

    private void validateSuccessfulGetResult(PatientDocument patientDocument, MvcResult mvcResult)
            throws JsonProcessingException, UnsupportedEncodingException {
        PatientDTO patientDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                PatientDTO.class);
        Map<String, Object> systemValues = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<HashMap<String, Object>>() {
                });

        assertEquals(patientDocument.getId(), systemValues.get("id"));
        assertEquals(patientDocument.getUserId(), patientDTO.getUserId());
        assertEquals(patientDocument.getFirstName(), patientDTO.getFirstName());
        assertEquals(patientDocument.getLastName(), patientDTO.getLastName());
        assertEquals(patientDocument.getBirthDate(), patientDTO.getBirthDate());
        assertEquals(patientDocument.getPhoneNumber(), patientDTO.getPhoneNumber());
        assertEquals(patientDocument.getFamilyConnectionId(), patientDTO.getFamilyConnectionId());
        assertEquals(patientDocument.getIin(), patientDTO.getIin());
        assertEquals(patientDocument.getCreatedAt().toEpochSecond(),
                OffsetDateTime.parse(systemValues.get("createdAt").toString(), dateTimeFormatter).toEpochSecond());
    }

    @Test
    void getPatientsByUserId_returnsUnauthorized_whenUserUnauthorized() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void getPatientsByUserId_returnsForbidden_whenUserIsNotOwner() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void getPatientsByUserId_returnsPatients_whenUserIsOwner() throws Exception {
        // given
        PatientDocument patientDocument1 = getPatientEntity();
        PatientDocument patientDocument2 = getPatientEntity();
        patientDocument1.setId(null);
        patientDocument2.setId(null);
        patientDocument1 = patientRepository.save(patientDocument1);
        patientDocument2 = patientRepository.save(patientDocument2);
        List<PatientDocument> patientDocumentList = List.of(patientDocument1, patientDocument2);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/"
                                + patientDocument1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientDocument1.getUserId())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetListResult(patientDocumentList, mvcResult);
    }

    @Test
    void getPatientsByUserId_returnsPatients_underDoctorRole() throws Exception {
        // given
        PatientDocument patientDocument1 = getPatientEntity();
        PatientDocument patientDocument2 = getPatientEntity();
        patientDocument1.setId(null);
        patientDocument2.setId(null);
        patientDocument1 = patientRepository.save(patientDocument1);
        patientDocument2 = patientRepository.save(patientDocument2);
        List<PatientDocument> patientDocumentList = List.of(patientDocument1, patientDocument2);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/"
                                + patientDocument1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetListResult(patientDocumentList, mvcResult);
    }

    @Test
    void getPatientsByUserId_returnsPatients_underOrganizationRole() throws Exception {
        // given
        PatientDocument patientDocument1 = getPatientEntity();
        PatientDocument patientDocument2 = getPatientEntity();
        patientDocument1.setId(null);
        patientDocument2.setId(null);
        patientDocument1 = patientRepository.save(patientDocument1);
        patientDocument2 = patientRepository.save(patientDocument2);
        List<PatientDocument> patientDocumentList = List.of(patientDocument1, patientDocument2);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/"
                                + patientDocument1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_ORGANIZATION)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetListResult(patientDocumentList, mvcResult);
    }

    private static void validateSuccessfulGetListResult(List<PatientDocument> patientDocumentList, MvcResult mvcResult)
            throws JsonProcessingException, UnsupportedEncodingException {
        List<PatientDTO> patientDTOList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        List<Map<String, Object>> systemValuesList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertFalse(patientDTOList.isEmpty());
        assertEquals(patientDocumentList.size(), patientDTOList.size());

        for (int i = 0; i < patientDocumentList.size(); i++) {
            PatientDocument entity = patientDocumentList.get(i);
            PatientDTO dto = patientDTOList.get(i);
            Map<String, Object> systemValues = systemValuesList.get(i);

            assertEquals(entity.getId(), systemValues.get("id"));
            assertEquals(entity.getUserId(), dto.getUserId());
            assertEquals(entity.getFirstName(), dto.getFirstName());
            assertEquals(entity.getLastName(), dto.getLastName());
            assertEquals(entity.getBirthDate(), dto.getBirthDate());
            assertEquals(entity.getPhoneNumber(), dto.getPhoneNumber());
            assertEquals(entity.getFamilyConnectionId(), dto.getFamilyConnectionId());
            assertEquals(entity.getIin(), dto.getIin());
            assertEquals(entity.getCreatedAt().toEpochSecond(),
                    OffsetDateTime.parse(systemValues.get("createdAt").toString(), dateTimeFormatter).toEpochSecond());
        }
    }

    @Test
    void deletePatientById_returnsUnauthorized_whenUserUnauthorized() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void deletePatientById_returnsForbidden_underOrganizationRole() throws Exception {
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_ORGANIZATION)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void deletePatientById_returnsForbidden_whenUserIsNotOwner() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void deletePatientById_returnsBadRequest_whenInvalidId() throws Exception {
        // given
        UUID invalidId = UUID.randomUUID();
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/"
                                + invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isBadRequest()).andReturn();
        // then
        ErrorResponseDTO errorResponseDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                ErrorResponseDTO.class);

        assertNotNull(errorResponseDTO.getDateTime());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponseDTO.getCode());
        assertEquals(MessageSource.PATIENT_BY_ID_NOT_FOUND.getText(invalidId.toString()),
                errorResponseDTO.getMessage());
    }

    @Test
    void deletePatientById_deletesPatient_underPatientRole() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientDocument.getUserId())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNoContent()).andReturn();
    }

    @Test
    void deletePatientById_deletesPatient_underDoctorRole() throws Exception {
        // given
        PatientDocument patientDocument = getPatientEntity();
        patientDocument = patientRepository.save(patientDocument);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + patientDocument.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientDocument.getUserId())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNoContent()).andReturn();
    }
}
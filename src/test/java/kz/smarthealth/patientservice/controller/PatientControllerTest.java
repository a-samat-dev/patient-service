package kz.smarthealth.patientservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.smarthealth.patientservice.exception.CustomException;
import kz.smarthealth.patientservice.model.dto.ErrorResponseDTO;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.dto.UserRole;
import kz.smarthealth.patientservice.model.entity.PatientEntity;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
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
class PatientControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT);

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
        patientDTO.setUpdatedAt(null);
        patientDTO.setCreatedBy(null);
        patientDTO.setUpdatedBy(null);
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
        patientDTO.setUpdatedAt(null);
        patientDTO.setCreatedBy(null);
        patientDTO.setUpdatedBy(null);
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
        patientDTO.setUpdatedAt(null);
        patientDTO.setCreatedBy(null);
        patientDTO.setUpdatedBy(null);
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
        patientDTO.setUpdatedAt(null);
        patientDTO.setCreatedBy(null);
        patientDTO.setUpdatedBy(null);
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
        patientDTO.setUpdatedAt(null);
        patientDTO.setCreatedBy(null);
        patientDTO.setUpdatedBy(null);
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
        assertNotNull(systemValues.get("createdBy"));
        assertNotNull(systemValues.get("updatedAt"));
        assertNotNull(systemValues.get("updatedBy"));
        assertNull(systemValues.get("deletedAt"));

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
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/" + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void getPatientById_returnsForbidden_whenUserIsNotOwner() throws Exception {
        // given
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/" + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isForbidden()).andReturn();
    }

    @Test
    void getPatientById_returnsPatient_whenUserIsOwner() throws Exception {
        // given
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/"
                                + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientEntity.getUserId())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetResult(patientEntity, mvcResult);
    }

    @Test
    void getPatientById_returnsPatient_underDoctorRole() throws Exception {
        // given
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/"
                                + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetResult(patientEntity, mvcResult);
    }

    @Test
    void getPatientById_returnsPatient_underOrganizationRole() throws Exception {
        // given
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/"
                                + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_ORGANIZATION)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetResult(patientEntity, mvcResult);
    }

    private void validateSuccessfulGetResult(PatientEntity patientEntity, MvcResult mvcResult)
            throws JsonProcessingException, UnsupportedEncodingException {
        PatientDTO patientDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                PatientDTO.class);
        Map<String, Object> systemValues = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<HashMap<String, Object>>() {
                });

        assertEquals(patientEntity.getId().toString(), systemValues.get("id"));
        assertEquals(patientEntity.getUserId(), patientDTO.getUserId());
        assertEquals(patientEntity.getFirstName(), patientDTO.getFirstName());
        assertEquals(patientEntity.getLastName(), patientDTO.getLastName());
        assertEquals(patientEntity.getBirthDate(), patientDTO.getBirthDate());
        assertEquals(patientEntity.getPhoneNumber(), patientDTO.getPhoneNumber());
        assertEquals(patientEntity.getFamilyConnectionId(), patientDTO.getFamilyConnectionId());
        assertEquals(patientEntity.getIin(), patientDTO.getIin());
        assertEquals(dateTimeFormatter.format(patientEntity.getCreatedAt()), systemValues.get("createdAt"));
        assertEquals(dateTimeFormatter.format(patientEntity.getUpdatedAt()), systemValues.get("updatedAt"));
        assertEquals(patientEntity.getCreatedBy(), systemValues.get("createdBy"));
        assertEquals(patientEntity.getUpdatedBy(), systemValues.get("updatedBy"));
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
        PatientEntity patientEntity1 = getPatientEntity();
        PatientEntity patientEntity2 = getPatientEntity();
        patientEntity1 = patientRepository.save(patientEntity1);
        patientEntity2 = patientRepository.save(patientEntity2);
        List<PatientEntity> patientEntityList = List.of(patientEntity1, patientEntity2);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/"
                                + patientEntity1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientEntity1.getUserId())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetListResult(patientEntityList, mvcResult);
    }

    @Test
    void getPatientsByUserId_returnsPatients_underDoctorRole() throws Exception {
        // given
        PatientEntity patientEntity1 = getPatientEntity();
        PatientEntity patientEntity2 = getPatientEntity();
        patientEntity1 = patientRepository.save(patientEntity1);
        patientEntity2 = patientRepository.save(patientEntity2);
        List<PatientEntity> patientEntityList = List.of(patientEntity1, patientEntity2);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/"
                                + patientEntity1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetListResult(patientEntityList, mvcResult);
    }

    @Test
    void getPatientsByUserId_returnsPatients_underOrganizationRole() throws Exception {
        // given
        PatientEntity patientEntity1 = getPatientEntity();
        PatientEntity patientEntity2 = getPatientEntity();
        patientEntity1 = patientRepository.save(patientEntity1);
        patientEntity2 = patientRepository.save(patientEntity2);
        List<PatientEntity> patientEntityList = List.of(patientEntity1, patientEntity2);
        // when
        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/patients/by-user-id/"
                                + patientEntity1.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", UUID.randomUUID())
                        .header("role", UserRole.ROLE_ORGANIZATION)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk()).andReturn();
        // then
        validateSuccessfulGetListResult(patientEntityList, mvcResult);
    }

    private static void validateSuccessfulGetListResult(List<PatientEntity> patientEntityList, MvcResult mvcResult)
            throws JsonProcessingException, UnsupportedEncodingException {
        List<PatientDTO> patientDTOList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        List<Map<String, Object>> systemValuesList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertFalse(patientDTOList.isEmpty());
        assertEquals(patientEntityList.size(), patientDTOList.size());

        for (int i = 0; i < patientEntityList.size(); i++) {
            PatientEntity entity = patientEntityList.get(i);
            PatientDTO dto = patientDTOList.get(i);
            Map<String, Object> systemValues = systemValuesList.get(i);

            assertEquals(entity.getId().toString(), systemValues.get("id"));
            assertEquals(entity.getUserId(), dto.getUserId());
            assertEquals(entity.getFirstName(), dto.getFirstName());
            assertEquals(entity.getLastName(), dto.getLastName());
            assertEquals(entity.getBirthDate(), dto.getBirthDate());
            assertEquals(entity.getPhoneNumber(), dto.getPhoneNumber());
            assertEquals(entity.getFamilyConnectionId(), dto.getFamilyConnectionId());
            assertEquals(entity.getIin(), dto.getIin());
            assertEquals(dateTimeFormatter.format(entity.getCreatedAt()), systemValues.get("createdAt"));
            assertEquals(dateTimeFormatter.format(entity.getUpdatedAt()), systemValues.get("updatedAt"));
            assertEquals(entity.getCreatedBy(), systemValues.get("createdBy"));
            assertEquals(entity.getUpdatedBy(), systemValues.get("updatedBy"));
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
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + patientEntity.getId())
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
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientEntity.getUserId())
                        .header("role", UserRole.ROLE_PATIENT)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNoContent()).andReturn();
    }

    @Test
    void deletePatientById_deletesPatient_underDoctorRole() throws Exception {
        // given
        PatientEntity patientEntity = getPatientEntity();
        patientEntity = patientRepository.save(patientEntity);
        // when
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/patients/" + patientEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("userId", patientEntity.getUserId())
                        .header("role", UserRole.ROLE_DOCTOR)
                        .characterEncoding("utf-8"))
                .andExpect(status().isNoContent()).andReturn();
    }
}
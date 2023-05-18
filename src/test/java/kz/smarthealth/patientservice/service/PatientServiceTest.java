package kz.smarthealth.patientservice.service;

import kz.smarthealth.patientservice.exception.CustomException;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.entity.PatientEntity;
import kz.smarthealth.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static kz.smarthealth.patientservice.util.MessageSource.PATIENT_BY_ID_NOT_FOUND;
import static kz.smarthealth.patientservice.util.TestData.getPatientDTO;
import static kz.smarthealth.patientservice.util.TestData.getPatientEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PatientService}
 *
 * Created by Samat Abibulla on 2023-03-14
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private PatientService underTest;

    @Test
    void savePatient_returnsPatient() {
        // given
        PatientDTO expectedPatientDTO = getPatientDTO();
        expectedPatientDTO.setId(null);
        PatientEntity patientEntity = getPatientEntity();
        when(patientRepository.save(any())).thenReturn(patientEntity);
        // when
        PatientDTO actualPatientDTO = underTest.savePatient(expectedPatientDTO);
        // then
        assertNotNull(actualPatientDTO.getId());
        assertEquals(expectedPatientDTO.getUserId(), actualPatientDTO.getUserId());
        assertEquals(expectedPatientDTO.getFirstName(), actualPatientDTO.getFirstName());
        assertEquals(expectedPatientDTO.getLastName(), actualPatientDTO.getLastName());
        assertEquals(expectedPatientDTO.getBirthDate(), actualPatientDTO.getBirthDate());
        assertEquals(expectedPatientDTO.getPhoneNumber(), actualPatientDTO.getPhoneNumber());
        assertEquals(expectedPatientDTO.getFamilyConnectionId(), actualPatientDTO.getFamilyConnectionId());
        assertEquals(expectedPatientDTO.getIin(), actualPatientDTO.getIin());
        assertNotNull(actualPatientDTO.getCreatedAt());
        assertNotNull(actualPatientDTO.getUpdatedAt());
        assertNotNull(actualPatientDTO.getCreatedBy());
        assertNotNull(actualPatientDTO.getUpdatedBy());
    }

    @Test
    void getPatientById_throwsException_whenPatientNotFound() {
        // given
        UUID patientId = UUID.randomUUID();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.getPatientById(patientId));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.name(), exception.getError());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.getText(patientId.toString()), exception.getErrorMessage());
    }

    @Test
    void getPatientById_returnsPatient() {
        // given
        PatientEntity patientEntity = getPatientEntity();
        when(patientRepository.findById(patientEntity.getId())).thenReturn(Optional.of(patientEntity));
        // when
        PatientDTO patientDTO = underTest.getPatientById(patientEntity.getId());
        // then
        assertEquals(patientEntity.getId(), patientDTO.getId());
        assertEquals(patientEntity.getUserId(), patientDTO.getUserId());
        assertEquals(patientEntity.getFirstName(), patientDTO.getFirstName());
        assertEquals(patientEntity.getLastName(), patientDTO.getLastName());
        assertEquals(patientEntity.getBirthDate(), patientDTO.getBirthDate());
        assertEquals(patientEntity.getPhoneNumber(), patientDTO.getPhoneNumber());
        assertEquals(patientEntity.getFamilyConnectionId(), patientDTO.getFamilyConnectionId());
        assertEquals(patientEntity.getIin(), patientDTO.getIin());
        assertEquals(patientEntity.getCreatedAt(), patientDTO.getCreatedAt());
        assertEquals(patientEntity.getUpdatedAt(), patientDTO.getUpdatedAt());
        assertEquals(patientEntity.getCreatedBy(), patientDTO.getCreatedBy());
        assertEquals(patientEntity.getUpdatedBy(), patientDTO.getUpdatedBy());
    }

    @Test
    void getPatientsByUserId_returnsPatients() {
        // given
        PatientEntity patientEntity1 = getPatientEntity();
        PatientEntity patientEntity2 = getPatientEntity();
        patientEntity2.setId(UUID.randomUUID());
        List<PatientEntity> patientEntityList = List.of(patientEntity1, patientEntity2);
        when(patientRepository.findAllByUserId(patientEntity1.getUserId()))
                .thenReturn(patientEntityList);
        // when
        List<PatientDTO> actualPatientList = underTest.getPatientsByUserId(patientEntity1.getUserId());
        // then
        assertFalse(actualPatientList.isEmpty());

        for (int i = 0; i < patientEntityList.size(); i++) {
            PatientEntity entity = patientEntityList.get(0);
            PatientDTO dto = actualPatientList.get(0);

            assertEquals(entity.getId(), dto.getId());
            assertEquals(entity.getUserId(), dto.getUserId());
            assertEquals(entity.getFirstName(), dto.getFirstName());
            assertEquals(entity.getLastName(), dto.getLastName());
            assertEquals(entity.getBirthDate(), dto.getBirthDate());
            assertEquals(entity.getPhoneNumber(), dto.getPhoneNumber());
            assertEquals(entity.getFamilyConnectionId(), dto.getFamilyConnectionId());
            assertEquals(entity.getIin(), dto.getIin());
            assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
            assertEquals(entity.getUpdatedAt(), dto.getUpdatedAt());
            assertEquals(entity.getCreatedBy(), dto.getCreatedBy());
            assertEquals(entity.getUpdatedBy(), dto.getUpdatedBy());
        }
    }

    @Test
    void deletePatientById_throwsException_whenPatientNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deletePatientById(id));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.name(), exception.getError());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.getText(id.toString()), exception.getErrorMessage());
    }

    @Test
    void deletePatientById_throwsException_whenUserIsNotOwner() {
        // given
        UUID userId = UUID.randomUUID();
        PatientEntity patientEntity = getPatientEntity();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(userId.toString());
        when(patientRepository.findById(patientEntity.getId())).thenReturn(Optional.of(patientEntity));
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.deletePatientById(patientEntity.getId()));
        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        reset(authentication);
        reset(securityContext);
    }

    @Test
    void deletePatientById_deletesPatient() {
        // given
        ArgumentCaptor<PatientEntity> argumentCaptor = ArgumentCaptor.forClass(PatientEntity.class);
        PatientEntity patientEntity = getPatientEntity();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(patientEntity.getUserId().toString());
        when(patientRepository.findById(patientEntity.getId())).thenReturn(Optional.of(patientEntity));
        // when
        underTest.deletePatientById(patientEntity.getId());
        // then
        verify(patientRepository).delete(argumentCaptor.capture());
        PatientEntity actualPatientEntity = argumentCaptor.getValue();

        assertNotNull(actualPatientEntity.getId());
        reset(authentication);
        reset(securityContext);
    }
}
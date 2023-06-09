package kz.smarthealth.patientservice.service;

import kz.smarthealth.patientservice.exception.CustomException;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.entity.PatientDocument;
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
        PatientDocument patientDocument = getPatientEntity();
        when(patientRepository.save(any())).thenReturn(patientDocument);
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
    }

    @Test
    void getPatientById_throwsException_whenPatientNotFound() {
        // given
        String patientId = UUID.randomUUID().toString();
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> underTest.getPatientById(patientId));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.name(), exception.getError());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.getText(patientId), exception.getErrorMessage());
    }

    @Test
    void getPatientById_returnsPatient() {
        // given
        PatientDocument patientDocument = getPatientEntity();
        when(patientRepository.findById(patientDocument.getId())).thenReturn(Optional.of(patientDocument));
        // when
        PatientDTO patientDTO = underTest.getPatientById(patientDocument.getId());
        // then
        assertEquals(patientDocument.getId(), patientDTO.getId());
        assertEquals(patientDocument.getUserId(), patientDTO.getUserId());
        assertEquals(patientDocument.getFirstName(), patientDTO.getFirstName());
        assertEquals(patientDocument.getLastName(), patientDTO.getLastName());
        assertEquals(patientDocument.getBirthDate(), patientDTO.getBirthDate());
        assertEquals(patientDocument.getPhoneNumber(), patientDTO.getPhoneNumber());
        assertEquals(patientDocument.getFamilyConnectionId(), patientDTO.getFamilyConnectionId());
        assertEquals(patientDocument.getIin(), patientDTO.getIin());
        assertEquals(patientDocument.getCreatedAt(), patientDTO.getCreatedAt());
    }

    @Test
    void getPatientsByUserId_returnsPatients() {
        // given
        PatientDocument patientDocument1 = getPatientEntity();
        PatientDocument patientDocument2 = getPatientEntity();
        patientDocument2.setId(UUID.randomUUID().toString());
        List<PatientDocument> patientDocumentList = List.of(patientDocument1, patientDocument2);
        when(patientRepository.findAllByUserId(patientDocument1.getUserId())).thenReturn(patientDocumentList);
        // when
        List<PatientDTO> actualPatientList = underTest.getPatientsByUserId(patientDocument1.getUserId());
        // then
        assertFalse(actualPatientList.isEmpty());

        for (int i = 0; i < patientDocumentList.size(); i++) {
            PatientDocument entity = patientDocumentList.get(0);
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
        }
    }

    @Test
    void deletePatientById_throwsException_whenPatientNotFound() {
        // given
        String id = UUID.randomUUID().toString();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deletePatientById(id));
        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.name(), exception.getError());
        assertEquals(PATIENT_BY_ID_NOT_FOUND.getText(id), exception.getErrorMessage());
    }

    @Test
    void deletePatientById_throwsException_whenUserIsNotOwner() {
        // given
        UUID userId = UUID.randomUUID();
        PatientDocument patientDocument = getPatientEntity();
        String patientId = patientDocument.getId();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(userId.toString());
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patientDocument));
        // when
        CustomException exception = assertThrows(CustomException.class, () -> underTest.deletePatientById(patientId));
        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        reset(authentication);
        reset(securityContext);
    }

    @Test
    void deletePatientById_deletesPatient() {
        // given
        ArgumentCaptor<PatientDocument> argumentCaptor = ArgumentCaptor.forClass(PatientDocument.class);
        PatientDocument patientDocument = getPatientEntity();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(patientDocument.getUserId());
        when(patientRepository.findById(patientDocument.getId())).thenReturn(Optional.of(patientDocument));
        // when
        underTest.deletePatientById(patientDocument.getId());
        // then
        verify(patientRepository).delete(argumentCaptor.capture());
        PatientDocument actualPatientDocument = argumentCaptor.getValue();

        assertNotNull(actualPatientDocument.getId());
        reset(authentication);
        reset(securityContext);
    }
}
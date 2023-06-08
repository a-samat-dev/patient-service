package kz.smarthealth.patientservice.service;

import kz.smarthealth.patientservice.aop.Log;
import kz.smarthealth.patientservice.exception.CustomException;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.entity.PatientDocument;
import kz.smarthealth.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static kz.smarthealth.patientservice.util.MessageSource.PATIENT_BY_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    /**
     * Creates new patient
     *
     * @param patientDTO patient data
     * @return newly created patient
     */
    @Log
    public PatientDTO savePatient(PatientDTO patientDTO) {
        PatientDocument patientDocument = modelMapper.map(patientDTO, PatientDocument.class);
        patientDocument.setCreatedAt(OffsetDateTime.now());
        patientDocument = patientRepository.save(patientDocument);

        return modelMapper.map(patientDocument, PatientDTO.class);
    }

    /**
     * Fetches patient by id
     *
     * @param id patient id
     * @return existing patient
     */
    @Log
    public PatientDTO getPatientById(String id) {
        PatientDocument patientDocument = getPatientEntityById(id);

        return modelMapper.map(patientDocument, PatientDTO.class);
    }

    /**
     * Fetches list of patients by user id
     *
     * @param userId user id
     * @return list of patients
     */
    public List<PatientDTO> getPatientsByUserId(String userId) {
        return patientRepository.findAllByUserId(userId).stream()
                .map(document -> modelMapper.map(document, PatientDTO.class))
                .toList();
    }

    /**
     * Deletes patient by id
     *
     * @param id patient id
     */
    public void deletePatientById(String id) {
        PatientDocument patientDocument = getPatientEntityById(id);
        String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!authenticatedUserId.equals(patientDocument.getUserId())) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .build();
        }

        patientRepository.delete(patientDocument);
    }

    /**
     * Retrieves patient from DB
     *
     * @param id patient id
     * @return patient
     */
    private PatientDocument getPatientEntityById(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .error(PATIENT_BY_ID_NOT_FOUND.name())
                        .errorMessage(PATIENT_BY_ID_NOT_FOUND.getText(id))
                        .build());
    }
}

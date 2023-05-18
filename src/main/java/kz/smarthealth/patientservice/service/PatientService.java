package kz.smarthealth.patientservice.service;

import kz.smarthealth.patientservice.aop.Log;
import kz.smarthealth.patientservice.exception.CustomException;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.entity.PatientEntity;
import kz.smarthealth.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
        PatientEntity patientEntity = modelMapper.map(patientDTO, PatientEntity.class);
        patientEntity = patientRepository.save(patientEntity);

        return modelMapper.map(patientEntity, PatientDTO.class);
    }

    /**
     * Fetches patient by id
     *
     * @param id patient id
     * @return existing patient
     */
    @Log
    public PatientDTO getPatientById(UUID id) {
        PatientEntity patientEntity = getPatientEntityById(id);

        return modelMapper.map(patientEntity, PatientDTO.class);
    }

    /**
     * Fetches list of patients by user id
     *
     * @param userId user id
     * @return list of patients
     */
    public List<PatientDTO> getPatientsByUserId(UUID userId) {
        return patientRepository.findAllByUserId(userId).stream()
                .map(entity -> modelMapper.map(entity, PatientDTO.class))
                .toList();
    }

    /**
     * Deletes patient by id
     *
     * @param id patient id
     */
    public void deletePatientById(UUID id) {
        PatientEntity patientEntity = getPatientEntityById(id);
        String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!authenticatedUserId.equals(patientEntity.getUserId().toString())) {
            throw CustomException.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .build();
        }

        patientRepository.delete(patientEntity);
    }

    /**
     * Retrieves patient from DB
     *
     * @param id patient id
     * @return patient
     */
    private PatientEntity getPatientEntityById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> CustomException.builder()
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .error(PATIENT_BY_ID_NOT_FOUND.name())
                        .errorMessage(PATIENT_BY_ID_NOT_FOUND.getText(id.toString()))
                        .build());
    }
}

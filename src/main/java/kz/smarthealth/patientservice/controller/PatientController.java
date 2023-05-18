package kz.smarthealth.patientservice.controller;

import jakarta.validation.Valid;
import kz.smarthealth.patientservice.aop.Log;
import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    /**
     * Creates new patient
     *
     * @param patientDTO patient data
     * @return newly created patient
     */
    @PostMapping
    @Secured({"ROLE_PATIENT", "ROLE_DOCTOR"})
    @Log
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDTO savePatient(@RequestBody @Valid PatientDTO patientDTO) {
        return patientService.savePatient(patientDTO);
    }

    /**
     * Fetches patient by id
     *
     * @param id patient id
     * @return existing patient
     */
    @GetMapping("/{id}")
    @Secured({"ROLE_ORGANIZATION", "ROLE_PATIENT", "ROLE_DOCTOR"})
    @PostAuthorize("returnObject != null and authentication.principal.username == returnObject.userId.toString() " +
            "or hasRole('ROLE_DOCTOR') or hasRole('ROLE_ORGANIZATION')")
    @Log
    public PatientDTO getPatientById(@PathVariable UUID id) {
        return patientService.getPatientById(id);
    }

    /**
     * Fetches list of patients by user id
     *
     * @param userId user id
     * @return list of patients
     */
    @GetMapping("/by-user-id/{userId}")
    @PreAuthorize("hasRole('ROLE_DOCTOR') or hasRole('ROLE_ORGANIZATION') " +
            "or (authenticated and authentication.principal.username == #userId.toString())")
    @Log
    public List<PatientDTO> getPatientsByUserId(@PathVariable UUID userId) {
        return patientService.getPatientsByUserId(userId);
    }

    /**
     * Deletes patient by id
     *
     * @param id patient id
     */
    @DeleteMapping("/{id}")
    @Secured({"ROLE_DOCTOR", "ROLE_PATIENT"})
    @Log
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatientById(@PathVariable UUID id) {
        patientService.deletePatientById(id);
    }
}

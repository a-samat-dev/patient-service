package kz.samat.patientservice.service

import kz.samat.patientservice.enum.MessageSource
import kz.samat.patientservice.exception.CustomException
import kz.samat.patientservice.model.Patient
import kz.samat.patientservice.repository.PatientRepository
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * Service class that works with patients
 *
 * Created by Samat Abibulla 2022/12/13
 */
@Service
@RequiredArgsConstructor
class PatientService @Autowired constructor(private val patientRepository: PatientRepository) {

    /**
     * Creates set of patients
     *
     * @param patientSet patients to be created
     * @return set of created patients
     */
    @Secured("ROLE_DOCTOR", "ROLE_PATIENT")
    fun createPatients(patientSet: List<Patient>): List<Patient> = patientRepository.saveAll(patientSet)

    /**
     * Returns patient by id
     *
     * @param id of patient
     * @throws CustomException if patient not found
     * @return patient found by id
     */
    @PostAuthorize("hasRole('ROLE_ADMIN') or returnObject.userId == authentication.principal.username")
    fun getPatientById(id: String): Patient =
        patientRepository.findById(id).orElseThrow {
            CustomException(
                HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.name,
                MessageSource.PATIENT_NOT_FOUND.getText(id)
            )
        }

    /**
     * Returns set of patients by user id
     *
     * @param userId id of user that own patients
     * @return set of patients
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.username")
    fun getPatientsByUserId(userId: String): List<Patient> = patientRepository.findAllByUserId(userId)

    fun deletePatientById(id: String) {
        val patient = getPatientById(id)

        if (patient.userId != SecurityContextHolder.getContext().authentication.name) {
            throw CustomException(
                HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.name,
                MessageSource.FORBIDDEN.getText()
            )
        }

        patientRepository.deleteById(id)
    }

    /**
     * Updates patient by id
     *
     * @param id of patient that must be updated
     * @param patient data that must be applied
     * @return updated patient data
     */
    @PostAuthorize("hasRole('ROLE_ADMIN') or returnObject.userId == authentication.principal.username")
    fun updatePatientById(id: String, patient: Patient): Patient {
        val p = getPatientById(id)
        patient.id = id
        patient.userId = p.userId

        return patientRepository.save(patient)
    }
}
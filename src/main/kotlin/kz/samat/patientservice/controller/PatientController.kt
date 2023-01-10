package kz.samat.patientservice.controller

import kz.samat.patientservice.model.Patient
import kz.samat.patientservice.service.PatientService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * REST api for patients CRUD operations
 *
 * Created by Samat Abibulla on 2022/12/13
 */
@RestController
@RequestMapping("/api/v1/patients")
class PatientController @Autowired constructor(private val patientService: PatientService) {

    /**
     * Creates set of patients
     *
     * @param patientSet patients to be created
     * @return set of created patients
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPatients(@RequestBody patientSet: List<Patient>): List<Patient> =
        patientService.createPatients(patientSet)

    /**
     * Returns patient by id
     *
     * @param id of patient
     * @return patient found by id
     */
    @GetMapping("/{id}")
    fun getPatientById(@PathVariable id: String): Patient = patientService.getPatientById(id)

    /**
     * Returns set of patients by user id
     *
     * @param userId id of user that own patients
     * @return set of patients
     */
    @GetMapping("/by-user/{userId}")
    fun getPatientsByUserId(
        @PathVariable("userId") userId: String
    ): List<Patient> = patientService.getPatientsByUserId(userId)

    /**
     * Deletes patient by id
     *
     * @param id of patient
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePatientById(@PathVariable id: String) = patientService.deletePatientById(id)

    /**
     * Updates patient by id
     *
     * @param id of patient that must be updated
     * @param patient data that must be applied
     * @return updated patient data
     */
    @PutMapping("/{id}")
    fun updatePatientById(@PathVariable id: String, @RequestBody patient: Patient): Patient =
        patientService.updatePatientById(id, patient)
}
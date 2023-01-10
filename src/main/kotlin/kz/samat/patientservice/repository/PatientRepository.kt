package kz.samat.patientservice.repository

import kz.samat.patientservice.model.Patient
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Repository for [Patient]
 *
 * Created by Samat Abibulla 2022/12/13
 */
@Repository
interface PatientRepository : MongoRepository<Patient, String> {

    /**
     * Returns set of patients by user id
     *
     * @param userId id of user that own patients
     * @return set of patients
     */
    fun findAllByUserId(userId: String): List<Patient>
}
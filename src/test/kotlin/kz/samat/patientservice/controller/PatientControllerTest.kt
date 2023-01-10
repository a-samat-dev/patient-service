package kz.samat.patientservice.controller

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kz.samat.patientservice.enum.MessageSource
import kz.samat.patientservice.exception.ErrorResponse
import kz.samat.patientservice.model.Patient
import kz.samat.patientservice.repository.PatientRepository
import kz.samat.patientservice.util.LocalDateConverter
import kz.samat.patientservice.util.OffsetDateTimeConverter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

/**
 * Integration tests for [PatientController]
 *
 * Created by Samat Abibulla on 2023-01-10
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.yml"])
class PatientControllerTest {

    private final val userId = UUID.randomUUID().toString()

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var patientRepository: PatientRepository

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate::class.java, LocalDateConverter())
        .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeConverter())
        .create();

    @Test
    fun createPatients() {
        // given
        val patient1 = Patient(
            name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
            userId = userId
        )
        val patient2 = Patient(
            name = "Patient2", lastName = "LastName2", birthDate = LocalDate.now().minusYears(4),
            userId = userId
        )
        val patients: List<Patient> = listOf(
            patient1, patient2
        )
        val requestBody = gson.toJson(patients)
        val type = object : TypeToken<List<Patient>>() {}.type
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/patients").contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isCreated).andReturn()
        // then
        val result = gson.fromJson<List<Patient>>(response.response.contentAsString, type)
        assertNotNull(result)
        assertFalse(result.isEmpty())

        for (i in 0 until patients.size) {
            assertNotNull(result[i].id)
            assertEquals(patients[i].name, result[i].name)
            assertEquals(patients[i].lastName, result[i].lastName)
            assertEquals(patients[i].birthDate, result[i].birthDate)
            assertEquals(patients[i].userId, result[i].userId)
        }
    }

    @Test
    fun getPatientById_returnsErrorResponse_whenPatientNotFound() {
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/patients/1").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isNotFound).andReturn()
        // then
        val result = gson.fromJson(response.response.contentAsString, ErrorResponse::class.java)

        assertNotNull(result)
        assertEquals(HttpStatus.NOT_FOUND.name, result.error)
        assertEquals("Patient with such id not found, id: 1", result.message)
    }

    @Test
    fun getPatientById_returnsPatient() {
        // given
        var patient = Patient(
            name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
            userId = userId
        )
        patient = patientRepository.save(patient)
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/patients/${patient.id}").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isOk).andReturn()
        // then
        val result = gson.fromJson(response.response.contentAsString, Patient::class.java)

        assertNotNull(result)
        assertEquals(patient.id, result.id)
        assertEquals(patient.name, result.name)
        assertEquals(patient.lastName, result.lastName)
        assertEquals(patient.birthDate, result.birthDate)
        assertEquals(patient.userId, result.userId)
    }

    @Test
    fun getPatientsByUserId() {
        // given
        val patient1 = Patient(
            name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
            userId = userId
        )
        val patient2 = Patient(
            name = "Patient2", lastName = "LastName2", birthDate = LocalDate.now().minusYears(4),
            userId = userId
        )
        patientRepository.save(patient1)
        patientRepository.save(patient2)
        val type = object : TypeToken<List<Patient>>() {}.type
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/patients/by-user/${userId}").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isOk).andReturn()
        // then
        val result: List<Patient> = gson.fromJson<List<Patient>>(response.response.contentAsString, type)

        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertEquals(2, result.size)
        assertNotNull(result[0].id)
        assertEquals(patient1.name, result[0].name)
        assertEquals(patient1.lastName, result[0].lastName)
        assertEquals(patient1.birthDate, result[0].birthDate)
        assertEquals(patient1.userId, result[0].userId)
        assertNotNull(result[1].id)
        assertEquals(patient2.name, result[1].name)
        assertEquals(patient2.lastName, result[1].lastName)
        assertEquals(patient2.birthDate, result[1].birthDate)
        assertEquals(patient2.userId, result[1].userId)
    }

    @Test
    fun deletePatientById_returnsErrorResponse_whenPatientNotFound() {
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/patients/1").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isNotFound).andReturn()
        // then
        val result = gson.fromJson(response.response.contentAsString, ErrorResponse::class.java)

        assertNotNull(result)
        assertEquals(HttpStatus.NOT_FOUND.name, result.error)
        assertEquals("Patient with such id not found, id: 1", result.message)
    }

    @Test
    fun deletePatientById_returnsErrorResponse_accessDenied() {
        // given
        val patient1 = patientRepository.save(
            Patient(
                name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
                userId = UUID.randomUUID().toString()
            )
        )
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/patients/${patient1.id}").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isForbidden).andReturn()
        // then
        val result = gson.fromJson(response.response.contentAsString, ErrorResponse::class.java)

        assertNotNull(result)
        assertEquals(HttpStatus.FORBIDDEN.name, result.error)
        assertEquals("You do not have access to commit this operation", result.message)
    }

    @Test
    fun deletePatientById_deletesPatient() {
        // given
        val patient1 = patientRepository.save(
            Patient(
                name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
                userId = userId
            )
        )
        // when
        this.mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/patients/${patient1.id}").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .characterEncoding("utf-8")
        ).andExpect(status().isNoContent)
    }

    @Test
    fun updatePatientById_returnsErrorResponse_whenPatientNotFound() {
        // given
        val patient = patientRepository.save(
            Patient(
                name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
                userId = userId
            )
        )
        patient.name = "Patient11"
        patient.lastName = "LastName11"
        patient.birthDate = LocalDate.now().minusYears(10)
        val requestBody = gson.toJson(patient)
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/patients/1").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .content(requestBody)
                .characterEncoding("utf-8")
        ).andExpect(status().isNotFound).andReturn()
        // then
        val result = gson.fromJson(response.response.contentAsString, ErrorResponse::class.java)

        assertNotNull(result)
        assertEquals(HttpStatus.NOT_FOUND.name, result.error)
        assertEquals(MessageSource.PATIENT_NOT_FOUND.getText("1"), result.message)
    }

    @Test
    fun updatePatientById_returnsErrorResponse_whenAccessDenied() {
        // given
        val patient = patientRepository.save(
            Patient(
                name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
                userId = UUID.randomUUID().toString()
            )
        )
        patient.name = "Patient11"
        patient.lastName = "LastName11"
        patient.birthDate = LocalDate.now().minusYears(10)
        val requestBody = gson.toJson(patient)
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/patients/${patient.id}").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .content(requestBody)
                .characterEncoding("utf-8")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun updatePatientById_updatesPatient() {
        // given
        val patient = patientRepository.save(
            Patient(
                name = "Patient1", lastName = "LastName1", birthDate = LocalDate.now().minusYears(5),
                userId = userId
            )
        )
        patient.name = "Patient11"
        patient.lastName = "LastName11"
        patient.birthDate = LocalDate.now().minusYears(10)
        val requestBody = gson.toJson(patient)
        // when
        val response = this.mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/patients/${patient.id}").contentType(MediaType.APPLICATION_JSON)
                .header("userId", userId)
                .header("role", "ROLE_PATIENT")
                .content(requestBody)
                .characterEncoding("utf-8")
        ).andExpect(status().isOk).andReturn()
        // then
        val result: Patient = gson.fromJson(response.response.contentAsString, Patient::class.java)

        assertNotNull(result)
    }
}
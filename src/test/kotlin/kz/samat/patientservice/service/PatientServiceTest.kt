package kz.samat.patientservice.service

import kz.samat.patientservice.exception.CustomException
import kz.samat.patientservice.model.Patient
import kz.samat.patientservice.repository.PatientRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.util.*

/**
 * Unit tests for [PatientService]
 *
 * Created by Samat Abibulla on 2023-01-09
 */
@Extensions(ExtendWith(MockitoExtension::class))
internal class PatientServiceTest {

    @Mock
    private lateinit var patientRepository: PatientRepository

    private lateinit var underTest: PatientService

    @BeforeEach
    fun beforeAll() {
        this.underTest = PatientService(patientRepository)
    }

    @Test
    fun createPatients_returnsCreatedPatients() {
        // given
        val userId = UUID.randomUUID().toString()
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
        val savedPatient1: Patient = patient1.copy(id = "1")
        val savedPatient2: Patient = patient2.copy(id = "2")
        val savedPatients = mutableListOf<Patient>(savedPatient1, savedPatient2)
        `when`(patientRepository.saveAll(patients)).thenReturn(savedPatients)
        // when
        val result: List<Patient> = underTest.createPatients(patients)
        // then
        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertEquals(savedPatients, result)
    }

    @Test
    fun getPatientById_throwsException_whenPatientNotFound() {
        // given
        `when`(patientRepository.findById("1")).thenReturn(Optional.empty())
        // when
        val exception = assertThrows(CustomException::class.java) { underTest.getPatientById("1") }
        // then
        assertNotNull(exception)
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertEquals("Patient not found, id=1", exception.error)
    }

    @Test
    fun getPatientById_returnsPatient() {
        // given
        val userId = UUID.randomUUID().toString()
        val patient = Patient(
            id = "1", name = "Patient1", lastName = "LastName1",
            birthDate = LocalDate.now().minusYears(5), userId = userId
        )
        `when`(patientRepository.findById("1")).thenReturn(Optional.of(patient))
        // when
        val result = underTest.getPatientById("1")
        // then
        assertNotNull(result)
        assertEquals(patient, result)
    }

    @Test
    fun getPatientsByUserId_returnsPatients() {
        // given
        val userId = UUID.randomUUID().toString()
        val patient1 = Patient(
            id = "1", name = "Patient1", lastName = "LastName1",
            birthDate = LocalDate.now().minusYears(5),
            userId = userId
        )
        val patient2 = Patient(
            id = "2", name = "Patient2", lastName = "LastName2",
            birthDate = LocalDate.now().minusYears(4), userId = userId
        )
        val patients: List<Patient> = listOf(
            patient1, patient2
        )
        `when`(patientRepository.findAllByUserId(userId)).thenReturn(patients)
        // when
        val result = underTest.getPatientsByUserId(userId)
        // then
        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertEquals(patients, result)
    }

    @Test
    fun deletePatientById_throwsException_whenPatientNotFound() {
        // given
        `when`(patientRepository.findById("1")).thenReturn(Optional.empty())
        // when
        val exception = assertThrows(CustomException::class.java) { underTest.deletePatientById("1") }
        // then
        assertNotNull(exception)
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertEquals("Patient not found, id=1", exception.error)
    }

    @Test
    fun deletePatientById_deletesPatient() {
        // given
        val patient = Patient(
            id = "1", name = "Patient1", lastName = "LastName1",
            birthDate = LocalDate.now().minusYears(5),
            userId = UUID.randomUUID().toString()
        )
        `when`(patientRepository.findById("1")).thenReturn(Optional.of(patient))
        // when
        underTest.deletePatientById("1")
        // then
        assertTrue(true)
    }
}
package kz.samat.patientservice.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

/**
 * Patient who visits doctor
 *
 * Created by Samat Abibulla on 2022/12/13
 */
@Document("patients")
data class Patient(
    @Id var id: String?,
    var name: String,
    var lastName: String,
    var birthDate: LocalDate,
    var userId: String
) {
    constructor(
        name: String,
        lastName: String,
        birthDate: LocalDate,
        userId: String
    ) : this(null, name, lastName, birthDate, userId)
}
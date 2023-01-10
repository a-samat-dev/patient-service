package kz.samat.patientservice.exception

import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

class ErrorResponse(
    val dateTime: OffsetDateTime,
    val status: HttpStatus,
    val code: Int,
    val error: String,
    val message: String,
    val invalidFields: Map<String, String?>
)
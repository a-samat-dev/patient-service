package kz.samat.patientservice.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val invalidFields: Map<String, String?> = ex.fieldErrors.map { it.field to it.defaultMessage }.toMap()
        val errorResponse = ErrorResponse(
            OffsetDateTime.now(), status, status.value(), "Validation Error",
            ex.message, invalidFields
        )

        return ResponseEntity(errorResponse, status)
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(OffsetDateTime.now(), ex.status, ex.status.value(), ex.error, ex.message, emptyMap())

        return ResponseEntity(errorResponse, ex.status)
    }
}
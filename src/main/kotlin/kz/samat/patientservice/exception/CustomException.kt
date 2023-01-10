package kz.samat.patientservice.exception

import org.springframework.http.HttpStatus

class CustomException(val status: HttpStatus, val error: String, override val message: String) : RuntimeException()
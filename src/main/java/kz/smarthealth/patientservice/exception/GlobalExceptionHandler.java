package kz.smarthealth.patientservice.exception;

import kz.smarthealth.patientservice.model.dto.ErrorResponseDTO;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> invalidFields = ex.getFieldErrors().stream()
                .collect(toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .dateTime(OffsetDateTime.now())
                .code(status.value())
                .message("Validation Error")
                .invalidFields(invalidFields)
                .build();

        return new ResponseEntity<>(errorResponseDTO, status);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex) {
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .dateTime(OffsetDateTime.now())
                .code(ex.getHttpStatus().value())
                .message(ex.getErrorMessage())
                .build();

        return new ResponseEntity<>(errorResponseDTO, ex.getHttpStatus());
    }
}

package kz.smarthealth.patientservice.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CustomException extends RuntimeException {

    private final HttpStatus httpStatus;

    private final String error;

    private final String errorMessage;
}

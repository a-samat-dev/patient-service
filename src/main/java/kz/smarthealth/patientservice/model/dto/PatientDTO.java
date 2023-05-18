package kz.smarthealth.patientservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kz.smarthealth.patientservice.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PatientDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull
    private UUID userId;

    @NotEmpty
    private String firstName;

    private String lastName;

    @NotNull
    @JsonFormat(pattern = AppConstants.DEFAULT_DATE)
    private LocalDate birthDate;

    private String phoneNumber;

    private Short familyConnectionId;

    private String iin;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected OffsetDateTime createdAt;

    @JsonFormat(shape = STRING, pattern = AppConstants.DEFAULT_OFFSET_DATE_TIME_FORMAT)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected OffsetDateTime updatedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected String createdBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    protected String updatedBy;
}
package kz.smarthealth.patientservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Document("patients")
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PatientDocument {

    @Id
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String phoneNumber;
    private Short familyConnectionId;
    private String iin;
    protected OffsetDateTime createdAt;
}

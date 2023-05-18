package kz.smarthealth.patientservice.util;

import kz.smarthealth.patientservice.model.dto.PatientDTO;
import kz.smarthealth.patientservice.model.entity.PatientEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TestData {

    public static final UUID PATIENT_ID = UUID.randomUUID();
    public static final UUID USER_ID = UUID.randomUUID();
    public static final String FIRST_NAME = "FirstName";
    public static final String LAST_NAME = "LastName";
    public static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);
    public static final String PHONE_NUMBER = "1234567890";
    public static final short FAMILY_CONNECTION_ID = (short) 1;
    public static final String IIN = "123456789012";

    public static PatientDTO getPatientDTO() {
        return PatientDTO.builder()
                .id(PATIENT_ID)
                .userId(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .birthDate(BIRTH_DATE)
                .phoneNumber(PHONE_NUMBER)
                .familyConnectionId(FAMILY_CONNECTION_ID)
                .iin(IIN)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy(USER_ID.toString())
                .updatedBy(USER_ID.toString())
                .build();
    }

    public static PatientEntity getPatientEntity() {
        return PatientEntity.builder()
                .id(PATIENT_ID)
                .userId(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .birthDate(BIRTH_DATE)
                .phoneNumber(PHONE_NUMBER)
                .familyConnectionId(FAMILY_CONNECTION_ID)
                .iin(IIN)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .createdBy(USER_ID.toString())
                .updatedBy(USER_ID.toString())
                .build();
    }
}

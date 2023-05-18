package kz.smarthealth.patientservice.util;

public enum MessageSource {

    PATIENT_BY_ID_NOT_FOUND("Patient with id=%s not found.");

    private final String text;

    MessageSource(String text) {
        this.text = text;
    }

    public String getText(String... params) {
        return String.format(this.text, params);
    }
}

package kz.samat.patientservice.enum

enum class MessageSource (val text: String) {

    PATIENT_NOT_FOUND("Patient with such id not found, id: %s"),
    FORBIDDEN("You do not have access to commit this operation");

    fun getText(vararg values: String) = String.format(this.text, *values)
}
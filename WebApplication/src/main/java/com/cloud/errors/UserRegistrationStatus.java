package com.cloud.errors;

public class UserRegistrationStatus {

    private String first_nameError;
    private String last_nameError;
    private String emailIdError;
    private String passwordError;

    public UserRegistrationStatus() {
        first_nameError = "-";
        last_nameError = "-";
        emailIdError = "-";
        passwordError = "-";
    }

    public UserRegistrationStatus(String first_nameError, String last_nameError, String emailIdError, String passwordError) {
        this.first_nameError = first_nameError;
        this.last_nameError = last_nameError;
        this.emailIdError = emailIdError;
        this.passwordError = passwordError;
    }

    public String getEmailIdError() {
        return emailIdError;
    }

    public void setEmailIdError(String emailIdError) {
        this.emailIdError = emailIdError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(String passwordError) {
        this.passwordError = passwordError;
    }

    public String getFirst_nameError() {
        return first_nameError;
    }

    public void setFirst_nameError(String first_nameError) {
        this.first_nameError = first_nameError;
    }

    public String getLast_nameError() {
        return last_nameError;
    }

    public void setLast_nameError(String last_nameError) {
        this.last_nameError = last_nameError;
    }
}

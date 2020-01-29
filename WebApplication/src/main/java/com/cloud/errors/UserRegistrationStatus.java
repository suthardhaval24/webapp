package com.cloud.errors;

public class UserRegistrationStatus {

    private String emailIdError;
    private String passwordError;

    public UserRegistrationStatus() {
        emailIdError = "-";
        passwordError = "-";
    }

    public UserRegistrationStatus(String emailIdError, String passwordError) {
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
}

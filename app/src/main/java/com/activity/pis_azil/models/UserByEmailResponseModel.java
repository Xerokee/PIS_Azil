package com.activity.pis_azil.models;

import java.util.List;

public class UserByEmailResponseModel {
    private UserModel result;
    private List<ErrorMessage> errorMessages;

    public UserModel getResult() {
        return result;
    }

    public void setResult(UserModel result) {
        this.result = result;
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public static class ErrorMessage {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

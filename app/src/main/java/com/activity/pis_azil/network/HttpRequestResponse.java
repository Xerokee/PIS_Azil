package com.activity.pis_azil.network;

import java.util.List;

public class HttpRequestResponse<T> {
    private T result;
    private List<ErrorMessage> errorMessages;

    // Getters and setters
    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
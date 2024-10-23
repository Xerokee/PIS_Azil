package com.activity.pis_azil.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HttpRequestResponseList<T> {
    @SerializedName("result")
    private List<T> result;
    @SerializedName("errorMessages")
    private List<ErrorMessage> errorMessages;

    public HttpRequestResponseList(List<T> r,List<ErrorMessage> em ){
        this.result=r;
        this.errorMessages=em;
    }

    public List<T> getResult() {return result;}
    public void setResult(List<T> result){this.result = result ;}
    public List<ErrorMessage> getErrorMessages() {return errorMessages;}
    public void setErrorMessages(List<ErrorMessage> errorMessages){this.errorMessages = errorMessages ;}
}

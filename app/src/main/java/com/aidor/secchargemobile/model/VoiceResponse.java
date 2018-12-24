package com.aidor.secchargemobile.model;

/**
 * Created by Shruti on 23/12/18.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class VoiceResponse {

    @SerializedName("response")
    @Expose
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
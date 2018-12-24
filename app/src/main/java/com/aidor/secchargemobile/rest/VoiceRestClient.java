package com.aidor.secchargemobile.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Shruti on 23/12/18.
 */

public class VoiceRestClient {

    static String API_BASE_URL = "http://192.168.1.60:8061/seccharge/";

    private  static Retrofit retrofit=null;

    public static Retrofit getClient()
    {
        // to accept malformed JSON
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        if (retrofit==null){
            retrofit= new Retrofit.Builder().baseUrl(API_BASE_URL).addConverterFactory( GsonConverterFactory.create(gson))
                    .build();

        }
        return retrofit;
    }
}

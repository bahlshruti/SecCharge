package com.aidor.secchargemobile.rest;


import com.aidor.secchargemobile.api.ReservationApi;
import com.aidor.secchargemobile.seccharge.MyURL;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class RestClientReservation {
    private static ReservationApi REST_CLIENT;
    private static String ROOT =  new MyURL().getUrlR();

    static {
        setupRestClient();
    }

    private RestClientReservation() {}

    public static ReservationApi get() {
        return REST_CLIENT;
    }

    private static void setupRestClient() {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(ROOT)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();
        REST_CLIENT = restAdapter.create(ReservationApi.class);
    }
}

package com.aidor.secchargemobile.rest;

import com.aidor.secchargemobile.api.ReservationApi;
import com.aidor.secchargemobile.api.RestApi;
import com.aidor.secchargemobile.seccharge.MyURL;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by sahajarora1286 on 12-05-2016.
 */
public class SecchargeRestClient {

    private static RestApi REST_CLIENT;
    private static String ROOT = new MyURL().getUrlSecharge();

    static {
        setupRestClient();
    }

    public SecchargeRestClient() {}

    public static RestApi get() {
        return REST_CLIENT;
    }

    private static void setupRestClient() {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(ROOT)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();
        REST_CLIENT = restAdapter.create(RestApi.class);
    }
}

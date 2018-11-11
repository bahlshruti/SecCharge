package com.aidor.secchargemobile.seccharge;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class RestClientMap {
    private static DurationApi REST_CLIENT;
    private static String ROOT =
            "https://maps.googleapis.com";

    static {
        setupRestClient();
    }

    private RestClientMap() {}

    public static DurationApi get() {
        return REST_CLIENT;
    }

    private static void setupRestClient() {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(ROOT)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();
        REST_CLIENT = restAdapter.create(DurationApi.class);
    }
}

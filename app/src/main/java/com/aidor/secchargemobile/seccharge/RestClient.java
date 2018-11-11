package com.aidor.secchargemobile.seccharge;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;


public class RestClient {
    private static BeforeApi REST_CLIENT;
    private static MyURL url = new MyURL();
    private static String ROOT = url.getMapURL_ChargSite_ID();
            //"http://.2.12:8011/seccharge";

    static {
        setupRestClient();
    }

    private RestClient() {}

    public static BeforeApi get() {
        return REST_CLIENT;
    }

    private static void setupRestClient() {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(ROOT)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();
        REST_CLIENT = restAdapter.create(BeforeApi.class);
    }
}


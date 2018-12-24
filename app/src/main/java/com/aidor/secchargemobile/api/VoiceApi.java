package com.aidor.secchargemobile.api;

/**
 * Created by Shruti on 23/12/18.
 */

import com.aidor.secchargemobile.model.VoiceResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface VoiceApi {

    @GET("test/voice/user/{command}")
    Call<VoiceResponse> sendUserCommand(@Path("command") String command);

    @GET("test/voice/auto_steer/{command}")
    Call<VoiceResponse> sendAutoSteeringCommand(@Path("command") String command);

    @GET("test/voice/auto/{command}")
    Call<VoiceResponse> sendAutoCommand(@Path("command") String command);
}

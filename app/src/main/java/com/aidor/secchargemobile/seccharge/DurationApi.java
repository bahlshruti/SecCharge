package com.aidor.secchargemobile.seccharge;


import com.aidor.secchargemobile.model.DurationResult;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface DurationApi {
    @GET("/maps/api/directions/json")
    void getNewDuration(@Query("origin") String origin, @Query("destination") String destination,
                    Callback<DurationResult> callback);
}

package com.aidor.secchargemobile.seccharge;

import com.aidor.secchargemobile.model.EditVehicleResponse;
import com.aidor.secchargemobile.model.ProfileResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface BeforeApi {
    @GET("/ChargingSites/{siteId}")
    void getDetails(@Path("siteId") String id,
                    Callback<DetailResponse> callback);

    @GET("/test/login/basicprofile")
    void getProfileData(@Query("userid") String userId, Callback<ProfileResponse> profileCallback);

    @GET("/test/register/vehicledataid")
    void getEditVehicleData(@Query("userid") String userId, @Query("id") String vehicleId, Callback<EditVehicleResponse> editVehicleCallback);
}

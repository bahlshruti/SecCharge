package com.aidor.secchargemobile.api;



import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.CsSitesResponse;
import com.aidor.secchargemobile.model.StatusModel;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by sahajarora1286 on 12-05-2016.
 */
public interface RestApi {
    @GET("/getLatLongIdsWithInRadius/{lat}/{lan}/{radius}")
    void getLatLongIdsWithInRadius(@Query("lat") Double lat, @Query("long") Double lon, @Query("radius") int radius, Callback<List<CsModel>> csModels);




}

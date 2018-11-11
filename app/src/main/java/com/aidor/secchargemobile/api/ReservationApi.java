package com.aidor.secchargemobile.api;

import com.aidor.secchargemobile.model.BatteryStatusModel;
import com.aidor.secchargemobile.model.CardInfoTest;
import com.aidor.secchargemobile.model.ChargingNotification;
import com.aidor.secchargemobile.model.CsModel;
import com.aidor.secchargemobile.model.DischargeStatusModel;
import com.aidor.secchargemobile.model.EditReservationModel;
import com.aidor.secchargemobile.model.Example;
import com.aidor.secchargemobile.model.MeterValueModel;
import com.aidor.secchargemobile.model.Reservationdetail;
import com.aidor.secchargemobile.model.ReserveTimeSlotTest;
import com.aidor.secchargemobile.model.ServerTimeModel;
import com.aidor.secchargemobile.model.StartChargingModel;
import com.aidor.secchargemobile.model.StatusModel;
import com.aidor.secchargemobile.model.UpdatedSocModel;
import com.aidor.secchargemobile.model.ViewReserveModel;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface
ReservationApi {
    @GET("/reserve")
    void getReserveView(@Query("siteID") String siteId, @Query("userid") String userId,
    Callback<ViewReserveModel> callback);


    @GET("/viewReserve")
    void getMyReservation(@Query("userid") String userId,
                          Callback<Example> callback);

    @GET("/reservationHistrory")
    void getMyReservationHistory(@Query("userid") String userId,
                                 Callback<Example> callback);

    @GET("/getCurrentServerTime")
    void getCurrentServerTime(Callback<ServerTimeModel> callback);

    @GET("/reserveNow")
    void getReserveNowStatus(Callback<StatusModel> statusModel);

    @GET("/getChargingSites")
    void getChargingSites(Callback<List<CsModel>> csModels);

    @GET("/remoteStopTransaction")
    void getRemoteStopStatus(@Query("Transaction_id") Long Transaction_id, Callback<StatusModel> statusModel);

    @GET("/startCharging")
    void getStartCharging(@Query("userid") String userId, Callback<StartChargingModel> callback);

    @GET("/remoteStartTransaction")
    void getBatteryStatus(@Query("userId") String userId, @Query("reservation_id") String reservationId,
                          Callback<BatteryStatusModel> callback);

    @GET("/updateSoc")
    void getUpdatedSOC(@Query("userid") String userId, Callback<UpdatedSocModel> callback);

    @GET("/editReserve")
    void getEditReservation(@Query("reservationId") String reservationId, @Query("reservedate") String reservationDate,
                            @Query("reservestarttime") String reservationStartTime, @Query("reserve_endtime") String reservationEndTime,
                            Callback<EditReservationModel> callback);

    @GET("/preauthorizedReservationId")
    void getReservationIdForCancelation(@Query("userid") String userId, Callback<Reservationdetail> callback);

    @GET("/isServerOpen")
    void getServerStatus(Callback<UpdatedSocModel> callback);

    @GET("/soc/getstatus1")
    void getMeterValues(@Query("userId") String userId, Callback<MeterValueModel> callback);

    @GET("/notifyChargingTime")
    void getChargingNotification(@Query("userId") String userId, Callback<ChargingNotification> notification);

    @GET("/discharge")
    void startDischarging(@Query("userId") String userId, Callback<DischargeStatusModel> dischargeStatusModelCallback);

    @GET("/searchStationVoice")
    void getCsSiteWithinRadius(@Query("lat") Double lat, @Query("lan") Double lan, @Query("radius") int radius, Callback<List<CsModel>> csModels);

    @GET("/reserveVoice")
    void getReserveViewVoice(@Query("siteID") String siteId, @Query("userid") String userId,
                             Callback<ReserveTimeSlotTest> callback);

    @POST("/reservationConfirmVoice")
    void postReservationConfirm(@Query("BtnConfirm") String BtnConfirm, @Query("registrationid")
            String registrationid, @Query("userid") String userId,
                                Callback<CardInfoTest> callback);

    @POST("/paymentVoice")
    void doPayment(@Query("BtnPay") String BtnPay, @Query("selectedSavedCard")
            String selectedSavedCard, @Query("myReservationIdInPayForm") String reservationId,
                   Callback<String> callback);

}

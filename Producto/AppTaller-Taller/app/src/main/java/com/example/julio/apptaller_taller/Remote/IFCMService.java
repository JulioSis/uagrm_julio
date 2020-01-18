package com.example.julio.apptaller_taller.Remote;



import com.example.julio.apptaller_taller.Model.DataMessage;
import com.example.julio.apptaller_taller.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Julio on 19/09/2018.
 */

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAaq3jy7o:APA91bFQtQ2T2KPqF1N2HT3cJBxriK30_WfrvkOPWvuvlZCzGTI-f1v6LH9-_UPDLmHUSMKiqa1kB7lFmTnVb7XTssEz8pCGtZ6Vns3naXX3JRGM2t6RvdES7senku6sfx-uLAxBZjTs"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}

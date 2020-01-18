package com.example.julio.apptaller_taller.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Julio on 18/09/2018.
 */

public interface GoogleApi {
    @GET
    Call<String> getPath(@Url String url);
}

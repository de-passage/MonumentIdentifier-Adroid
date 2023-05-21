package com.example.watchapplication;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HeritageApi {
    @GET("api/api.php?action=search&format=json")
    Call<MonumentResponse> getMonuments(@Query("bbox") Box bbox);

    @GET("api/api.php?action=search&format=json")
    Call<MonumentResponse> getMonuments(@Query("coord") Coordinate coord, @Query("radius") double radius);
}


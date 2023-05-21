package com.example.watchapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MonumentService {
    private static final String BASE_URL = "https://heritage.toolforge.org/";
    private final HeritageApi api;

    public class MonumentServiceBuilder {
        private final Coordinate coordinate;
        private final double radius;

        private BiConsumer<Call<MonumentResponse>, Response<MonumentResponse>> onResponse;
        private BiConsumer<Call<MonumentResponse>, Throwable> onFailure;

        public MonumentServiceBuilder(Coordinate coordinate, double radius) {
            this.coordinate = coordinate;
            this.radius = radius;
        }

        public MonumentServiceBuilder onResponse(BiConsumer<Call<MonumentResponse>,
                Response<MonumentResponse>> onResponse) {
            this.onResponse = onResponse;
            return this;
        }

        public MonumentServiceBuilder onFailure(BiConsumer<Call<MonumentResponse>,
                Throwable> onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        public void request() {
            Log.i("MonumentService", "Requesting monuments");
            api.getMonuments(coordinate, radius).enqueue(new Callback<MonumentResponse>() {
                @Override
                public void onResponse(@NonNull Call<MonumentResponse> call,
                                       @NonNull Response<MonumentResponse> response) {
                    if (onResponse != null ) {
                        onResponse.accept(call, response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MonumentResponse> call, @NonNull Throwable t) {
                    if (onFailure != null) {
                        onFailure.accept(call, t);
                    }
                }
            });
        }
    }

    public MonumentService() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();
        api = retrofit.create(HeritageApi.class);
    }

    public MonumentServiceBuilder getMonuments(final Coordinate coord, final double radius) {
        return new MonumentServiceBuilder(coord, radius);
    }
}


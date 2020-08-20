package com.example.ormroom;

import android.graphics.ColorSpace;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class DaggerNetModule {
    @Provides
    Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    };

    @Provides
    Call<List<RetrofitModel>> getCall(Retrofit retrofit) {
        IRestAPIforUser restAPIforUser = retrofit.create(IRestAPIforUser.class);
        return restAPIforUser.loadUsers();
    }
}

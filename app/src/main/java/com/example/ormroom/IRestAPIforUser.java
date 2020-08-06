package com.example.ormroom;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IRestAPIforUser {
    @GET("users/{user}")
    Call<RetrofitModel> loadUser1(@Path("user") String user);

}

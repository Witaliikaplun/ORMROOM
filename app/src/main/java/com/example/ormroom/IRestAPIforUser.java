package com.example.ormroom;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IRestAPIforUser {
    @GET("users/{user}")
    Call<RetrofitModel> loadUser1(@Path("user") String user);

    @GET("users")
    Call<List<RetrofitModel>> loadUsers();

}

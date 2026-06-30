package com.example.myapplication.network;

import com.example.myapplication.data.model.BaseResponse;
import com.example.myapplication.data.model.CameraListResponse;
import com.example.myapplication.data.model.LoginResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("app/login")
    Observable<BaseResponse<LoginResponse>> login(
            @Field("LoginForm[phone]") String phone,
            @Field("LoginForm[password]") String password
    );

    @FormUrlEncoded
    @POST("camera/info2")
    Observable<BaseResponse<CameraListResponse>> getCameraList(
            @Field("access-token") String token,
            @Field("UserID") String userId,
            @Field("page") int page,
            @Field("size") int size
    );
}

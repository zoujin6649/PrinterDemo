package com.example.printer.internet;

import com.example.printer.internet.bean.CommonResponse;
import com.example.printer.internet.bean.ListDeviceResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by claire on 2020/9/14.
 */
public interface IWifi {
    @POST
    @FormUrlEncoded
    Observable<ListDeviceResponse> getListDevices(@Url String url,
                                                  @Field("reqTime") String reqTime,
                                                  @Field("memberCode") String memberCode,
                                                  @Field("securityCode") String securityCode);

    @POST
    @FormUrlEncoded
    Observable<CommonResponse> addDevice(@Url String url,
                                         @Field("reqTime") String reqTime,
                                         @Field("securityCode") String securityCode,
                                         @Field("memberCode") String memberCode,
                                         @Field("deviceID") String deviceID,
                                         @Field("devName") String devName);

    @POST
    @FormUrlEncoded
    Observable<CommonResponse> deleteDevice(@Url String url,
                                            @Field("reqTime") String reqTime,
                                            @Field("securityCode") String securityCode,
                                            @Field("memberCode") String memberCode,
                                            @Field("deviceID") String deviceID);

    @POST
    @FormUrlEncoded
    Observable<CommonResponse> sendMsg(@Url String url,
                                       @Field("reqTime") String reqTime,
                                       @Field("securityCode") String securityCode,
                                       @Field("memberCode") String memberCode,
                                       @Field("deviceID") String deviceID,
                                       @Field("mode") String mode,//model 2-3,2自由格式打印,推荐，3十六进制命字符串打印
                                       @Field("msgDetail") String msgDetail);
}

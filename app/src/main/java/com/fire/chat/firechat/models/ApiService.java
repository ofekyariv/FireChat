package com.fire.chat.firechat.models;

import com.fire.chat.firechat.notifications.MyResponse;
import com.fire.chat.firechat.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGNgSlRo:APA91bFZgiu2d9t3u22EWES8_tIseExif9HOly6B3uctktjD_XecdLQQO8yoZYEWtlkmdxEW194u7kbXLPlIbOEF2M8jiHlqMF-opMv30TO3OtA7xAPGxnLIjFlIoZg9cPLOoLTLeQI9"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}

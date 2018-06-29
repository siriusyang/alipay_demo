package com.alipay.sdk.pay.demo.util;


import android.util.Log;

import com.alipay.sdk.pay.demo.PaymentParams;
import com.google.gson.Gson;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by bao.yang on 6/29/2018.
 */

public class OkHttpUtil {
    private static OkHttpClient client;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    {
        client = generateRetrofitOkhttpClient();
    }

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static String getSingeInfo(String url, PaymentParams payPar) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        String payment_params = new Gson().toJson(payPar);
        Log.e("payment_params", payment_params);
        RequestBody body = RequestBody.create(mediaType, payment_params);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("groupId", "0")
                .addHeader("companyId", "0")
                .addHeader("userId", "0")
                .addHeader("userName", "0")
                .addHeader("paymentType", "ALIPAY_APP")
                .addHeader("tradeType", "SIGN")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "ec0c1136-cf81-49ef-9d94-29db617ef010")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * generates the okhttp client used for retrofit services
     *
     * @return
     */
    protected OkHttpClient generateRetrofitOkhttpClient() {
        final OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(35, TimeUnit.SECONDS)
                .writeTimeout(35, TimeUnit.SECONDS)
                .readTimeout(35, TimeUnit.SECONDS);

        httpClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));

        return httpClientBuilder.build();
    }
}

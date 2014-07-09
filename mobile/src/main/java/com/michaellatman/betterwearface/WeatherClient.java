package com.michaellatman.betterwearface;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by michael on 7/8/14.
 */
public class WeatherClient {
    private static final String BASE_URL = "http://api.worldweatheronline.com/free/v1/weather.ashx";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(RequestParams params, JsonHttpResponseHandler responseHandler) {
        params.add("key","0e10ae68bfcd1e2df8bbad9ef8158eec4013d98f");
        params.add("format","json");
        client.get(getAbsoluteUrl(), params, responseHandler);
    }

    public static void post( RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(), params, responseHandler);
    }

    private static String getAbsoluteUrl() {
        return BASE_URL;
    }
}

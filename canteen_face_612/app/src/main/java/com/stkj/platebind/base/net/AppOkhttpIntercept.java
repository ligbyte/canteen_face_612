package com.stkj.platebind.base.net;

import androidx.annotation.NonNull;

import com.stkj.platebind.BuildConfig;
import com.stkj.platebind.login.helper.LoginHelper;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * app 的 okhttp 拦截器
 */
public class AppOkhttpIntercept implements Interceptor {
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        requestBuilder.addHeader("Domain", AppNetManager.INSTANCE.getDeviceDomain());
        requestBuilder.addHeader("deviceId", AppNetManager.INSTANCE.getDeviceId());
        requestBuilder.addHeader("shopId", AppNetManager.INSTANCE.getShopId());
        requestBuilder.addHeader("token", LoginHelper.INSTANCE.getToken());
        requestBuilder.addHeader("deviceType", BuildConfig.deviceType);
        requestBuilder.addHeader("versionCode", BuildConfig.VERSION_NAME);
        requestBuilder.addHeader("buildId", BuildConfig.GIT_SHA);
        requestBuilder.addHeader("buildTime", BuildConfig.BUILD_TIME);
        return chain.proceed(requestBuilder.build());
    }
}

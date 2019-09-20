package com.beesightsoft.caf.services.network;

import android.content.Context;

import com.beesightsoft.caf.services.common.RestMessageResponse;
import com.beesightsoft.caf.services.filter.Filter;
import com.beesightsoft.caf.services.filter.InterceptFilter;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import rx.Observable;

/**
 * Created by nhancao on 3/11/17.
 */
public interface NetworkProvider {

    boolean isDebug();

    boolean isNetworkAvailable();

    Context getContext();

    GsonBuilder createBuilder();

    NetworkProvider addDefaultHeader();

    NetworkProvider addHeader(String key, String value);

    NetworkProvider addFilter(InterceptFilter interceptFilter);

    NetworkProvider clearFilter();

    NetworkProvider enableFilter(boolean enableFilter);

    NetworkProvider enableCookie(boolean enableCookie);

    NetworkProvider addInterceptor(Interceptor interceptor);

    NetworkProvider addNetworkInterceptor(Interceptor interceptor);

    HttpLoggingInterceptor.Level getLevel();

    int getTimeout();

    <T> T provideApi(String baseUrl, Class<T> apiClass);

    <T> T provideApi(String baseUrl, Class<T> apiClass, boolean enableProgress);

    <TResponse extends RestMessageResponse<TResult>, TResult> Observable<TResult> transformResponse(
            Observable<TResponse> call);

    <TResponse extends RestMessageResponse<TResult>, TResult> Observable<TResult> transformResponse(
            Observable<TResponse> call, boolean enableFilter);

    <TResponse> Observable<TResponse> verifyResponse(Observable<TResponse> call);

    <TResponse> Observable<TResponse> verifyResponse(Observable<TResponse> call, boolean enableFilter);

    <TResponse> Filter<TResponse, Observable<TResponse>> getRootFilter();

    <TResponse> Filter<TResponse, Observable<TResponse>> getCommonFilter();
}

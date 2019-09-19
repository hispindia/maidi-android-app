package com.beesightsoft.caf.services.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.beesightsoft.caf.services.filter.Filter;
import com.google.gson.Gson;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by nhancao on 3/11/17.
 */

public abstract class AbstractNetworkProvider implements NetworkProvider {

    protected Context context;

    public AbstractNetworkProvider(Context context) {
        this.context = context;
    }

    protected abstract Gson gson();

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public int getTimeout() {
        return 120;
    }

    @Override
    public HttpLoggingInterceptor.Level getLevel() {
        return HttpLoggingInterceptor.Level.BASIC;
    }

    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public <TResponse> Observable<TResponse> verifyResponse(Observable<TResponse> call) {
        return call
                .observeOn(Schedulers.computation())
                .onExceptionResumeNext(Observable.empty())
                .flatMap(Observable::just);
    }

    @Override
    public <TResponse> Filter<TResponse, Observable<TResponse>> getRootFilter() {
        return null;
    }

    @Override
    public <TResponse> Filter<TResponse, Observable<TResponse>> getCommonFilter() {
        return null;
    }
}

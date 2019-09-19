package com.beesightsoft.caf.services.filter;

import com.beesightsoft.caf.exceptions.ApiThrowable;
import com.beesightsoft.caf.exceptions.ErrorCodes;
import com.beesightsoft.caf.services.network.NetworkProvider;

import rx.Observable;

/**
 * Created by nhancao on 4/16/17.
 */

public class NetworkFilter<T> implements Filter<Throwable, Observable<T>> {

    protected NetworkProvider networkProvider;

    public NetworkFilter(NetworkProvider networkProvider) {
        this.networkProvider = networkProvider;
    }

    @Override
    public Observable<T> execute(Throwable throwable) {
        if (!networkProvider.isNetworkAvailable()) {
            return Observable.error(ApiThrowable.from(ErrorCodes.NETWORK_NOT_AVAILABLE_ERROR,
                                                      "Network is not available"));
        }
        return Observable.error(throwable);
    }
}

package com.beesightsoft.caf.services.filter;

import rx.Observable;

/**
 * Created by nhancao on 4/16/17.
 */

public interface InterceptFilter {

    <T> Observable.Transformer<T, T> execute();

}

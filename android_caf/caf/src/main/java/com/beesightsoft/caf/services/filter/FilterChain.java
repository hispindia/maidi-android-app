package com.beesightsoft.caf.services.filter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by nhancao on 4/16/17.
 */

public class FilterChain {

    protected List<InterceptFilter> filters = new ArrayList<>();

    public <T> Observable<T> execute(Observable<T> target) {
        Observable<T> res = target;
        for (InterceptFilter filter : filters) {
            res = target.compose(filter.execute());
        }
        return res;
    }

    public void addFilter(InterceptFilter filter) {
        filters.add(filter);
    }

    public void clearFilter() {
        filters.clear();
    }
}

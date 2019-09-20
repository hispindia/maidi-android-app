package com.beesightsoft.caf.services.repository;

import com.beesightsoft.caf.models.Cursor;

import java.util.List;

/**
 * Created by kietnh on 9/19/2016.
 */
public interface Repository<V> {
    Repository<V> getAll(String cacheKey);

    Repository<V> withPolicy(LoadPolicy policy);

    Repository<V> withLoadCallback(LoadCallback<V> callback);

    Repository<V> withCursorPaging(Cursor cursor);

    List<V> execute() throws Exception;
}

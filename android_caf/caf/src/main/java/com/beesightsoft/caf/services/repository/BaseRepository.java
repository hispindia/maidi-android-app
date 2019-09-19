package com.beesightsoft.caf.services.repository;

import com.beesightsoft.caf.models.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kietnh on 9/19/2016.
 */
public abstract class BaseRepository<V> implements Repository<V> {
    private String cacheKey;
    private LoadPolicy loadPolicy;
    private LoadCallback<V> loadCallback;
    private Cursor cursor;
    private Page page;
    private Map<String, List<V>> cacheMap = new HashMap<>();

    public Repository<V> getAll(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    public Repository<V> withPolicy(LoadPolicy loadPolicy) {
        this.loadPolicy = loadPolicy;
        return this;
    }

    public Repository<V> withLoadCallback(LoadCallback<V> loadCallback) {
        this.loadCallback = loadCallback;
        return this;
    }

    public Repository<V> withCursorPaging(Cursor cursor) {
        this.cursor = cursor;
        this.page = null; //only allow one type of paging at 1 time
        return this;
    }

    public Repository<V> withPaging(Page page) {
        this.page = page;
        this.cursor = null; //only allow one type of paging at 1 time
        return this;
    }

    public List<V> execute() throws Exception {
        List<V> cacheDataSource = null;

        if (!cacheMap.containsKey(cacheKey) || loadPolicy.isForceReload()) {
            cacheDataSource = loadCallback.getAll();
            cacheMap.put(cacheKey, cacheDataSource);
        } else {
            cacheDataSource = cacheMap.get(cacheKey);
            if (cacheDataSource == null) cacheDataSource = new ArrayList<>();
        }

        return cacheDataSource;
    }
}

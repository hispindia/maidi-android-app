package com.beesightsoft.caf.services.repository;

import java.util.List;

/**
 * Created by kietnh on 9/19/2016.
 */
public interface LoadCallback<V> {
    List<V> getAll() throws Exception;
}

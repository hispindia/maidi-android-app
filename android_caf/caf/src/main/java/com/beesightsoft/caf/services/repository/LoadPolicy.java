package com.beesightsoft.caf.services.repository;

/**
 * Created by kietnh on 9/19/2016.
 */
public enum LoadPolicy {
    FROM_CACHE,
    FORCE_RELOAD;

    public boolean isForceReload() {
        return this.equals(FORCE_RELOAD);
    }
}

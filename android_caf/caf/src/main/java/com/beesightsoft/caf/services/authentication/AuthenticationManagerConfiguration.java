package com.beesightsoft.caf.services.authentication;

/**
 * Created by kietngo on 5/30/2016.
 */
public class AuthenticationManagerConfiguration {
    private String uniqueStorageKey;
    private boolean useStorage;

    private AuthenticationManagerConfiguration() {
    }

    public static AuthenticationManagerConfiguration init() {
        return new AuthenticationManagerConfiguration();
    }

    public String getUniqueStorageKey() {
        return uniqueStorageKey;
    }

    public AuthenticationManagerConfiguration useStorage(String uniqueStorageKey) {
        this.useStorage = true;
        this.uniqueStorageKey = uniqueStorageKey;
        return this;
    }

    public boolean isUseStorage() {
        return useStorage;
    }

    public AuthenticationManagerConfiguration enableStorage() {
        this.useStorage = true;
        return this;
    }

    public AuthenticationManagerConfiguration disableStorage() {
        this.useStorage = false;
        return this;
    }
}

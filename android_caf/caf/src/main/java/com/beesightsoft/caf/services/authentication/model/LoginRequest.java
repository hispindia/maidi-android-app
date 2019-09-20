package com.beesightsoft.caf.services.authentication.model;

/**
 * Created by kietngo on 5/30/2016.
 */
public interface LoginRequest {
    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);
}

package com.beesightsoft.caf.services.authentication.model;

/**
 * Created by MyPC on 5/25/2016.
 */
public interface LoginResponse {
    String getUsername();

    void setUsername(String username);

    String getEmail();

    void setEmail(String email);

    String getAccessToken();

    void setAccessToken(String accessToken);
}

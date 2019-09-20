package com.beesightsoft.caf.services.authentication.model;

/**
 * Created by kietnh on 6/17/2016.
 */
public interface LoginSocialRequest {
    String getAccessToken();

    void setAccessToken(String accessToken);

    String getPlatform();

    void setPlatform(String platform);
}

package com.beesightsoft.caf.services.authentication;

import com.beesightsoft.caf.services.authentication.model.LoginRequest;
import com.beesightsoft.caf.services.authentication.model.LoginResponse;
import com.beesightsoft.caf.services.authentication.model.LoginSocialRequest;

import rx.Observable;

/**
 * Created by kietngo on 4/11/2016.
 */
public interface AuthenticationManager<
        TUser extends LoginResponse,
        TLoginRequest extends LoginRequest,
        TLoginSocialRequest extends LoginSocialRequest
        > {
    boolean isAuthenticated();

    TUser getCurrentUser();

    void setCurrentUser(TUser currentUser);

    AuthenticationManagerConfiguration configure();

    boolean loadFromStorage();

    Observable<TUser> login(TLoginRequest loginRequest);

    Observable<TUser> loginSocial(TLoginSocialRequest loginRequest);

    Observable<String> logout();
}

package com.beesightsoft.caf.services.filter;

import com.beesightsoft.caf.services.authentication.AbstractAuthenticationManager;
import com.beesightsoft.caf.services.authentication.model.LoginRequest;
import com.beesightsoft.caf.services.authentication.model.LoginResponse;
import com.beesightsoft.caf.services.authentication.model.LoginSocialRequest;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by nhancao on 4/16/17.
 */

public class AuthenticationSuccessFilter<
        TUser extends LoginResponse,
        TLoginRequest extends LoginRequest,
        TLoginSocialRequest extends LoginSocialRequest
        >
        implements OutputFilter<Observable.Transformer<TUser, TUser>> {

    protected AbstractAuthenticationManager<TUser, TLoginRequest, TLoginSocialRequest> accountManager;

    public AuthenticationSuccessFilter(
            AbstractAuthenticationManager<TUser, TLoginRequest, TLoginSocialRequest> accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public Observable.Transformer<TUser, TUser> execute() {
        return userObservable -> userObservable
                .observeOn(Schedulers.computation())
                .flatMap(user -> {
                    accountManager.setCurrentUser(user);
                    return Observable.just(user);
                });
    }
}

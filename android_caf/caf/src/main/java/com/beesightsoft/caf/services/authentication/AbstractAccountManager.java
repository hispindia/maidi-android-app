package com.beesightsoft.caf.services.authentication;

import com.beesightsoft.caf.services.authentication.model.LoginRequest;
import com.beesightsoft.caf.services.authentication.model.LoginResponse;
import com.beesightsoft.caf.services.authentication.model.LoginSocialRequest;
import com.beesightsoft.caf.services.filter.AuthenticationClearFilter;
import com.beesightsoft.caf.services.filter.AuthenticationSuccessFilter;

import rx.Observable;

/**
 * Created by nhancao on 4/26/17.
 */

public abstract class AbstractAccountManager<
        TUser extends LoginResponse,
        TLoginRequest extends LoginRequest,
        TLoginSocialRequest extends LoginSocialRequest
        >
        extends AbstractAuthenticationManager<TUser, TLoginRequest, TLoginSocialRequest>
        implements AuthenticationManager<TUser, TLoginRequest, TLoginSocialRequest> {

    public AbstractAccountManager(
            AuthenticationManagerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Observable<TUser> login(TLoginRequest loginRequest) {
        return onLogin(loginRequest)
                .compose(new AuthenticationSuccessFilter<>(this).execute());
    }

    protected abstract Observable<TUser> onLogin(TLoginRequest loginRequest);

    @Override
    public Observable<TUser> loginSocial(TLoginSocialRequest loginRequest) {
        return onLoginSocial(loginRequest)
                .compose(new AuthenticationSuccessFilter<>(this).execute());
    }

    protected abstract Observable<TUser> onLoginSocial(TLoginSocialRequest loginRequest);

    @Override
    public Observable<String> logout() {
        return onLogout()
                .compose(new AuthenticationClearFilter<>(this).execute());
    }

    protected abstract Observable<String> onLogout();
}

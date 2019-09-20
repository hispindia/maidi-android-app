package com.beesightsoft.caf.services.authentication;

import com.beesightsoft.caf.services.authentication.model.LoginRequest;
import com.beesightsoft.caf.services.authentication.model.LoginResponse;
import com.beesightsoft.caf.services.authentication.model.LoginSocialRequest;
import com.orhanobut.hawk.Hawk;

/**
 * Created by kietngo on 4/11/2016.
 */
public abstract class AbstractAuthenticationManager<
        TUser extends LoginResponse,
        TLoginRequest extends LoginRequest,
        TLoginSocialRequest extends LoginSocialRequest
        >
        implements AuthenticationManager<TUser, TLoginRequest, TLoginSocialRequest> {

    protected TUser currentUser;
    protected AuthenticationManagerConfiguration configuration;

    public AbstractAuthenticationManager(AuthenticationManagerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public TUser getCurrentUser() {
        return this.currentUser;
    }

    @Override
    public void setCurrentUser(TUser currentUser) {
        this.currentUser = currentUser;
        if (configuration.isUseStorage() && Hawk.isBuilt()) {
            if (currentUser == null) {
                Hawk.remove(configuration.getUniqueStorageKey());
            } else {
                Hawk.put(configuration.getUniqueStorageKey(), currentUser);
            }
        }
    }

    @Override
    public boolean isAuthenticated() {
        return this.currentUser != null;
    }

    @Override
    public AuthenticationManagerConfiguration configure() {
        return this.configuration;
    }

    @Override
    public boolean loadFromStorage() {
        boolean canLoad = false;

        if (configuration.isUseStorage() && Hawk.isBuilt()) {
            try {
                TUser user = Hawk.get(configuration.getUniqueStorageKey());
                if (user != null) {
                    this.currentUser = user;
                    canLoad = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return canLoad;
    }

}

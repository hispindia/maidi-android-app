package com.app.maidi.domains.child_registration

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ChildRegistrationPresenter : BasePresenter<ChildRegistrationView>{

    var accountService: AccountService
    var networkProvider: NetworkProvider
    var disposable : Disposable? = null

    @Inject
    constructor(networkProvider: NetworkProvider, accountService: AccountService){
        this.networkProvider = networkProvider
        this.accountService = accountService
    }
}
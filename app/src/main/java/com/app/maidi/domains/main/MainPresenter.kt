package com.app.maidi.domains.main

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MainPresenter : BasePresenter<MainView> {

    var accountService: AccountService
    var networkProvider: NetworkProvider
    var disposable : Disposable? = null

    @Inject
    constructor(networkProvider: NetworkProvider, accountService: AccountService){
        this.networkProvider = networkProvider
        this.accountService = accountService
    }
}
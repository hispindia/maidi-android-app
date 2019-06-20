package com.app.maidi.domains.login

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LoginPresenter : BasePresenter<LoginView>{

    var accountService: AccountService
    var networkProvider: NetworkProvider
    var disposable : Disposable? = null

    @Inject
    constructor(networkProvider: NetworkProvider, accountService: AccountService){
        this.networkProvider = networkProvider
        this.accountService = accountService
    }

    fun login(username: String, password: String){
        disposable?.let {
            if(!it!!.isDisposed){
                it!!.dispose()
            }
        }

        accountService?.let {
            disposable = it.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate({

                })
                .subscribe({
                    view.getAccountInfo(it)
                }, {
                    view.getApiFailed(it)
                })
        }
    }
}
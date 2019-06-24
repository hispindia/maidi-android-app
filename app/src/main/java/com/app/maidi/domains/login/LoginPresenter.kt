package com.app.maidi.domains.login

import android.app.Activity
import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.models.database.User
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import com.app.maidi.utils.AppPreferences
import com.app.maidi.utils.Constants
import com.squareup.okhttp.HttpUrl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.sdk.network.Credentials
import org.hisp.dhis.android.sdk.network.Session
import javax.inject.Inject

class LoginPresenter : BasePresenter<LoginView>{

    var accountService: AccountService
    var networkProvider: NetworkProvider
    var activity: Activity
    var disposable : Disposable? = null

    @Inject
    constructor(networkProvider: NetworkProvider, accountService: AccountService, activity: Activity){
        this.networkProvider = networkProvider
        this.accountService = accountService
        this.activity = activity
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
                .doOnTerminate({})
                .subscribe({
                    view.getAccountInfo(it)
                    AppPreferences.getInstance(activity)!!.putUserAuthentication(username, password)
                }, {
                    view.getApiFailed(it)
                })
        }
    }
}
package com.app.maidi.domains.login

import android.app.Activity
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.models.database.User
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import com.app.maidi.utils.AppPreferences
import com.app.maidi.utils.Constants
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.squareup.okhttp.HttpUrl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.sdk.controllers.DhisService
import org.hisp.dhis.android.sdk.network.Credentials
import java.util.concurrent.TimeUnit
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
        DhisService.logInUser(HttpUrl.parse(Constants.DHIS2_SERVER_URL), Credentials(username, password))
        /*disposable?.let {
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
                    AppPreferences.getInstance()!!.putUserAuthentication(username, password)
                }, {
                    view.getApiFailed(it)
                })
        }*/
    }

    fun sendVerifyRequest(activity: Activity, phoneNumber : String, callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks){
        if(isViewAttached)
            view.showLoading()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            activity,
            callback
        )
    }

    fun resendVerifyToken(activity: Activity, phoneNumber: String, callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks, resendToken: PhoneAuthProvider.ForceResendingToken){
        if(isViewAttached)
            view.showLoading()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            activity,
            callback,
            resendToken
        )
    }

    fun signInWithVerifyCode(credential: PhoneAuthCredential){
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    view.signInWithVerifyCodeSuccess()
                }else{
                    view.getApiFailed(task.exception)
                }
            }
    }
}
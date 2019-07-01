package com.app.maidi.domains.login.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.login.LoginActivity
import com.app.maidi.domains.login.LoginPresenter
import com.app.maidi.domains.login.LoginView
import com.app.maidi.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class LoginPhoneNumberFragment : BaseFragment(){

    lateinit var loginPresenter: LoginPresenter
    lateinit var verificationChangeListener: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    @BindView(R.id.fragment_login_input_phone_et_phone_number)
    lateinit var etPhoneNumber: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        createPresenter()

        var viewGroup = (activity as LoginActivity).layoutInflater.inflate(R.layout.fragment_login_input_phone, container, false)
        ButterKnife.bind(this, viewGroup)
        verificationChangeListener = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {

            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                (activity as LoginActivity).hideLoading()
                Toast.makeText(activity, p0!!.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String?, resendToken: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(verificationId, resendToken)
                (activity as LoginActivity).hideLoading()
                var bundle: Bundle = Bundle()
                var loginOTPFragment = LoginOTPFragment()
                bundle.putString(Constants.PHONE_NUMBER, etPhoneNumber.text!!.toString())
                bundle.putString(Constants.VERIFICATION_ID, verificationId)
                bundle.putParcelable(Constants.RESEND_TOKEN, resendToken)
                loginOTPFragment.arguments = bundle
                (activity as LoginActivity).transformFragment(R.id.activity_login_fl_fragment, loginOTPFragment)
            }
        }
        return viewGroup
    }

    @OnClick(R.id.fragment_login_input_phone_btn_next)
    fun onNext(){
        if(!etPhoneNumber.text!!.isEmpty()){
            loginPresenter.sendVerifyRequest(activity as Activity, etPhoneNumber.text.toString(), verificationChangeListener)
        }
    }

    fun createPresenter(): LoginPresenter {
        this.loginPresenter = (activity as LoginActivity).loginPresenter
        return this.loginPresenter
    }

}
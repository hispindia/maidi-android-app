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
import com.app.maidi.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class LoginPhoneNumberFragment : BaseFragment(){

    lateinit var loginActivity: LoginActivity
    lateinit var loginPresenter: LoginPresenter
    lateinit var verificationChangeListener: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    @BindView(R.id.fragment_login_input_phone_et_phone_number)
    lateinit var etPhoneNumber: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        loginActivity = activity as LoginActivity
        createPresenter()

        var viewGroup = loginActivity.layoutInflater.inflate(R.layout.fragment_login_input_phone, container, false)
        ButterKnife.bind(this, viewGroup)
        verificationChangeListener = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {

            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                loginActivity.hideLoading()
                Toast.makeText(loginActivity, p0!!.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String?, resendToken: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(verificationId, resendToken)
                loginActivity.hideLoading()
                var bundle: Bundle = Bundle()
                var loginOTPFragment = LoginOTPFragment()
                bundle.putString(Constants.PHONE_NUMBER, etPhoneNumber.text!!.toString())
                bundle.putString(Constants.VERIFICATION_ID, verificationId)
                bundle.putParcelable(Constants.RESEND_TOKEN, resendToken)
                loginOTPFragment.arguments = bundle
                loginActivity.transformFragment(R.id.activity_login_fl_fragment, loginOTPFragment)
            }
        }
        return viewGroup
    }

    @OnClick(R.id.fragment_login_input_phone_btn_next)
    fun onNext(){
        loginActivity.hideKeyBoard(loginActivity)
        if(!etPhoneNumber.text!!.isEmpty()){
            if(Utils.isValidPhoneNumber(etPhoneNumber.text.toString())){
                var phoneNumberWithPrefix = Constants.PHONE_NUMBER_PREFIX + etPhoneNumber.text.toString()
                loginPresenter.sendVerifyRequest(loginActivity, phoneNumberWithPrefix, verificationChangeListener)
                return
            }

            Toast.makeText(loginActivity, resources.getString(R.string.phone_number_invalid), Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(loginActivity, resources.getString(R.string.phone_number_not_been_typed), Toast.LENGTH_LONG).show()
    }

    fun createPresenter(): LoginPresenter {
        this.loginPresenter = loginActivity.loginPresenter
        return this.loginPresenter
    }

}
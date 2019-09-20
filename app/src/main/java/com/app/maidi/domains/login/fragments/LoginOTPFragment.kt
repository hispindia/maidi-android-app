package com.app.maidi.domains.login.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.*
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.login.LoginActivity
import com.app.maidi.domains.login.LoginPresenter
import com.app.maidi.utils.Constants
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.squareup.otto.Subscribe
import org.hisp.dhis.android.sdk.persistence.Dhis2Application

class LoginOTPFragment : BaseFragment(){

    lateinit var loginPresenter: LoginPresenter
    lateinit var phoneNumber: String
    lateinit var verificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var verificationChangeListener: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    @BindView(R.id.fragment_login_input_otp_et_otp)
    lateinit var etOtp: EditText

    @BindView(R.id.fragment_login_input_otp_tv_dot_1)
    lateinit var tvDotOne: TextView

    @BindView(R.id.fragment_login_input_otp_tv_dot_2)
    lateinit var tvDotTwo: TextView

    @BindView(R.id.fragment_login_input_otp_tv_dot_3)
    lateinit var tvDotThree: TextView

    @BindView(R.id.fragment_login_input_otp_tv_dot_4)
    lateinit var tvDotFour: TextView

    @BindView(R.id.fragment_login_input_otp_tv_dot_5)
    lateinit var tvDotFive: TextView

    @BindView(R.id.fragment_login_input_otp_tv_dot_6)
    lateinit var tvDotSix: TextView

    @BindView(R.id.fragment_login_input_otp_btn_resend)
    lateinit var btnResend: TextView

    @BindView(R.id.fragment_login_input_otp_ll_error)
    lateinit var llError: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        createPresenter()

        var viewGroup = (activity as LoginActivity).layoutInflater.inflate(R.layout.fragment_login_input_otp, container, false)
        ButterKnife.bind(this, viewGroup)

        phoneNumber = arguments!!.getString(Constants.PHONE_NUMBER)
        verificationId = arguments!!.getString(Constants.VERIFICATION_ID)
        resendToken = arguments!!.getParcelable(Constants.RESEND_TOKEN)

        verificationChangeListener = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {

            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                (activity as LoginActivity).hideLoading()
                Toast.makeText(activity, p0!!.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String?, resendToken: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(verificationId, resendToken)
                (activity as LoginActivity).hideLoading()
            }
        }

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        Dhis2Application.bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        Dhis2Application.bus.unregister(this)
    }

    @Subscribe
    fun onVerifyCodeFailed(exception: FirebaseAuthInvalidCredentialsException){
        llError.visibility = View.VISIBLE

        if(etOtp.text!!.length >= 1)
            tvDotOne.setBackgroundResource(R.drawable.round_button_gray_red_stroke)

        if(etOtp.text!!.length >= 2)
            tvDotTwo.setBackgroundResource(R.drawable.round_button_gray_red_stroke)

        if(etOtp.text!!.length >= 3)
            tvDotThree.setBackgroundResource(R.drawable.round_button_gray_red_stroke)

        if(etOtp.text!!.length >= 4)
            tvDotFour.setBackgroundResource(R.drawable.round_button_gray_red_stroke)

        if(etOtp.text!!.length >= 5)
            tvDotFive.setBackgroundResource(R.drawable.round_button_gray_red_stroke)

        if(etOtp.text!!.length == 6)
            tvDotSix.setBackgroundResource(R.drawable.round_button_gray_red_stroke)
    }

    @OnClick(R.id.fragment_login_input_otp_btn_submit)
    fun onSubmitOTP(){
        (activity as LoginActivity).showLoading()

        var credential = PhoneAuthProvider.getCredential(verificationId, etOtp.text!!.toString())

        loginPresenter.signInWithPhoneAuthCredential(credential, phoneNumber)
    }

    @OnClick(R.id.fragment_login_input_otp_btn_resend)
    fun onBtnResendClicked(){
        (activity as LoginActivity).showLoading()
        var phoneNumberWithPrefix = Constants.PHONE_NUMBER_PREFIX + phoneNumber
        loginPresenter.resendVerifyToken(activity as Activity, phoneNumberWithPrefix, verificationChangeListener, resendToken)
    }

    @OnTextChanged(R.id.fragment_login_input_otp_et_otp)
    fun OnTextChanged(text: CharSequence){
        if(text!!.length >= 1)
            tvDotOne.text = "•"
        else tvDotOne.text = ""

        if(text!!.length >= 2)
            tvDotTwo.text = "•"
        else tvDotTwo.text = ""

        if(text!!.length >= 3)
            tvDotThree.text = "•"
        else tvDotThree.text = ""

        if(text!!.length >= 4)
            tvDotFour.text = "•"
        else tvDotFour.text = ""

        if(text!!.length >= 5)
            tvDotFive.text = "•"
        else tvDotFive.text = ""

        if(text!!.length == 6)
            tvDotSix.text = "•"
        else tvDotSix.text = ""

        tvDotOne.setBackgroundResource(R.drawable.round_button_gray_gray_stroke)
        tvDotTwo.setBackgroundResource(R.drawable.round_button_gray_gray_stroke)
        tvDotThree.setBackgroundResource(R.drawable.round_button_gray_gray_stroke)
        tvDotFour.setBackgroundResource(R.drawable.round_button_gray_gray_stroke)
        tvDotFive.setBackgroundResource(R.drawable.round_button_gray_gray_stroke)
        tvDotSix.setBackgroundResource(R.drawable.round_button_gray_gray_stroke)

        llError.visibility = View.GONE
    }

    @OnFocusChange(R.id.fragment_login_input_otp_et_otp)
    fun OnFocusChanged(isFocused: Boolean){
        if(isFocused){
            etOtp.setSelection(etOtp.text!!.length)
        }
    }

    fun createPresenter(): LoginPresenter {
        this.loginPresenter = (activity as LoginActivity).loginPresenter
        return this.loginPresenter
    }

}
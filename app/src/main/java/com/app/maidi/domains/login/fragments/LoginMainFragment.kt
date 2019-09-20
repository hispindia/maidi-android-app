package com.app.maidi.domains.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.login.LoginActivity
import com.app.maidi.domains.login.LoginPresenter
import com.google.android.material.textfield.TextInputEditText

class LoginMainFragment : BaseFragment() {

    lateinit var loginPresenter: LoginPresenter

    @BindView(R.id.fragment_login_main_et_username)
    protected lateinit var etUsername: TextInputEditText

    @BindView(R.id.fragment_login_main_et_password)
    protected lateinit var etPassword: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_login_main, container, false)
        ButterKnife.bind(this, viewGroup)
        return viewGroup
    }

    @OnClick(R.id.fragment_login_main_btn_login)
    fun onLoginButtonClicked(){
        (activity as LoginActivity).showLoading()

        var username = etUsername.text.toString()
        var password = etPassword.text.toString()

        loginPresenter.login(username, password, "", false)
    }

    @OnClick(R.id.fragment_login_main_btn_beneficiary_login)
    fun onBeneficiaryLoginButtonClicked(){
        (activity as LoginActivity).transformFragment(R.id.activity_login_fl_fragment, LoginPhoneNumberFragment())
    }

    fun createPresenter(): LoginPresenter {
        this.loginPresenter = (activity as LoginActivity).loginPresenter
        return this.loginPresenter
    }
}
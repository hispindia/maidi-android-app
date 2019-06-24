package com.app.maidi.domains.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.fragments.LoginInputUsernameFragment
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.models.database.User
import com.google.android.material.textfield.TextInputEditText
import javax.inject.Inject

class LoginActivity : BaseActivity<LoginView, LoginPresenter>(), LoginView{

    lateinit var application: MainApplication

    @Inject
    lateinit var loginPresenter: LoginPresenter

    @BindView(R.id.activity_login_et_username)
    lateinit var etUsername : TextInputEditText

    @BindView(R.id.activity_login_et_password)
    lateinit var etPassword : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)
    }

    override fun createPresenter(): LoginPresenter {
        application = getApplication() as MainApplication
        DaggerLoginComponent.builder()
            .appComponent(application.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return loginPresenter
    }

    @OnClick(R.id.activity_login_btn_login)
    fun onLoginButtonClicked(){
        var username = etUsername.text.toString()
        var password = etPassword.text.toString()

        loginPresenter.login(username, password)
    }

    @OnClick(R.id.activity_login_btn_beneficiary_login)
    fun onBeneficiaryLoginButtonClicked(){

    }

    override fun getAccountInfo(user: User) {
        Toast.makeText(this, user.displayName, Toast.LENGTH_SHORT).show()
    }

    override fun getApiFailed(throwable: Throwable) {
        Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
    }
}
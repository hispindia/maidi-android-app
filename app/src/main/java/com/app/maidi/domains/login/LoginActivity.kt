package com.app.maidi.domains.login

import android.os.Bundle
import android.widget.Toast
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.fragments.LoginInputUsernameFragment
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.models.database.User
import javax.inject.Inject

class LoginActivity : BaseActivity<LoginView, LoginPresenter>(), LoginView{

    lateinit var application: MainApplication

    @Inject
    lateinit var loginPresenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        transformFragment(R.id.activity_login_fl_fragment, LoginInputUsernameFragment.newInstance())
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

    override fun getAccountInfo(user: User) {
        Toast.makeText(this, user.displayName, Toast.LENGTH_SHORT).show()
    }

    override fun getApiFailed(throwable: Throwable) {
        Toast.makeText(this, throwable.message, Toast.LENGTH_SHORT).show()
    }
}
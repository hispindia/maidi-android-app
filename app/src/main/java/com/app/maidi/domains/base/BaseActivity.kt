package com.app.maidi.domains.base

import androidx.fragment.app.Fragment
import com.app.maidi.R
import com.app.maidi.domains.login.fragments.LoginInputUsernameFragment
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView

abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>> : MvpActivity<V, P>(){

    fun transformFragment(container: Int, V : Fragment){
        supportFragmentManager.beginTransaction()
            .replace(container, V)
            .commit()
    }
}
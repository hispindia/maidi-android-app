package com.app.maidi.domains.login

import com.app.maidi.domains.base.BaseView
import com.app.maidi.models.database.User

interface LoginView : BaseView{

    fun signInWithVerifyCodeSuccess(phoneNumber : String)
    fun getAccountInfo(user: User)
    fun getApiFailed(exception: Exception?)
}
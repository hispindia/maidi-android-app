package com.app.maidi.domains.login

import com.app.maidi.domains.base.BaseView
import com.app.maidi.models.database.User
import java.lang.Exception

interface LoginView : BaseView{

    fun getAccountInfo(user: User)
    fun getApiFailed(exception: Exception?)
}
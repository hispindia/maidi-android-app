package com.app.maidi.services.account

import com.app.maidi.models.database.User
import io.reactivex.Observable

interface AccountService {
    fun login(username: String, password: String) : Observable<User>

}
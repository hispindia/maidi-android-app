package com.app.maidi.services.account

import com.app.maidi.models.database.User
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap

interface AccountServiceApi {

    @GET("29/me/")
    fun getCurrentUserInfo(
        @Header("Authorization") base64Credentials: String,
        @QueryMap queryParams: Map<String, String>) : Observable<User>
}
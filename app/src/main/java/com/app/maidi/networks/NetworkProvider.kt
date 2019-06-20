package com.app.maidi.networks

import io.reactivex.Observable

interface NetworkProvider {

    fun <T : Any> provideApi(baseUrl : String, apiClass: Class<T>, isDebug: Boolean) : T

    fun <T : Any> transformResponse(call: Observable<T>) : Observable<T>

    fun isNetworkConnected() : Boolean

    fun setHeader(key: String, value: String)
}
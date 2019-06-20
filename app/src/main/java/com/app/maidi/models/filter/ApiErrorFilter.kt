package com.app.maidi.models.filter

import android.widget.Toast
import com.app.maidi.BuildConfig
import com.beesightsoft.caf.services.common.ProductFlavor
import com.beesightsoft.caf.services.filter.InterceptFilter
import com.beesightsoft.caf.services.log.LogService
import com.beesightsoft.caf.services.network.NetworkProvider
import io.reactivex.Observable

class ApiErrorFilter {

    fun <T : Any?> execute(input: Throwable): Observable<T> {
        return Observable.error(input)
    }
}
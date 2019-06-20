package com.app.maidi.networks

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.app.maidi.models.filter.ApiErrorFilter
import com.app.maidi.utils.Constants
import com.beesightsoft.caf.services.network.HttpLoggingInterceptor
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NetworkProviderImpl(var application: Application) : NetworkProvider {

    private var timeout: Long = 120
    private var headers: MutableMap<String, String> = HashMap<String, String>()

    override fun <T : Any> provideApi(baseUrl: String, apiClass: Class<T>, isDebug: Boolean): T {
        var builder = OkHttpClient.Builder()
        builder.connectTimeout(timeout, TimeUnit.SECONDS)
        builder.readTimeout(timeout, TimeUnit.SECONDS)
        builder.writeTimeout(timeout, TimeUnit.SECONDS)

        if(isDebug){
            var httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            builder.addInterceptor(httpLoggingInterceptor)
        }

        builder.addInterceptor({ chain ->
            var requestBuilder = chain.request().newBuilder()
            for(header in  headers){
                requestBuilder.addHeader(header.key, header.value)
            }

            if(!isNetworkConnected()){
                throw Exception("Network is not available")
            }

            chain.proceed(requestBuilder.build())
        })

        var okHttpClient = builder.build()

        var restAdapter = Retrofit.Builder()
            .baseUrl(Constants.DHIS2_SERVER_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(okHttpClient)
            .build()

        return restAdapter.create(apiClass)
    }

    override fun <T : Any> transformResponse(call: Observable<T>): Observable<T> {
        var response = call.observeOn(Schedulers.computation())
        response = response.onErrorResumeNext({t: Throwable -> ApiErrorFilter().execute(t) })
        var result = response.flatMap { Observable.just(it) }
        return result
    }

    override fun isNetworkConnected(): Boolean {
        var connectivityManager = (application.getSystemService(Context.CONNECTIVITY_SERVICE)) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
    }

    override fun setHeader(key: String, value: String){
        headers.put(key, value)
    }
}
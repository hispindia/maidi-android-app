package com.app.maidi.infrastructures

import android.app.Application
import com.app.maidi.BuildConfig
import com.app.maidi.infrastructures.scope.ApplicationScope
import com.app.maidi.models.filter.ApiErrorFilter
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.networks.NetworkProviderImpl
import com.app.maidi.services.account.AccountService
import com.app.maidi.services.account.AccountServiceApi
import com.app.maidi.services.account.DefaultAccountService
import com.app.maidi.utils.Constants
import com.beesightsoft.caf.services.log.DefaultLogService
import com.beesightsoft.caf.services.log.LogService
import dagger.Module
import dagger.Provides
import java.lang.Exception
import javax.inject.Singleton

@Module
class AppModules{

    var application: Application

    constructor(application: Application){
        this.application = application
    }

    @Provides
    fun provideApplication() : Application{
        return application
    }

    @Provides
    fun provideLogService() : LogService{
        var defaultLogService = DefaultLogService()
        try {
            defaultLogService.init(application, "")
        }catch (ex: Exception){
            ex.printStackTrace()
        }

        return defaultLogService
    }

    @Provides
    fun provideNetworkProvider() : NetworkProvider {
        return NetworkProviderImpl(application)
    }

    @Provides
    fun provideAccountService(networkProvider: NetworkProvider) : AccountService{
        var restServiceApi = provideNetworkProvider()
            .provideApi(Constants.DHIS2_SERVER_URL, AccountServiceApi::class.java, true)

        return DefaultAccountService(networkProvider, restServiceApi)
    }
}
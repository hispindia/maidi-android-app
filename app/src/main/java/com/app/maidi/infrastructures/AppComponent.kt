package com.app.maidi.infrastructures

import com.app.maidi.infrastructures.scope.ApplicationScope
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import dagger.Component
import javax.inject.Singleton

@ApplicationScope
@Component(modules = [AppModules::class])
interface AppComponent {
    fun networkProvider() : NetworkProvider
    fun accountService() : AccountService
}
package com.app.maidi

import android.app.Application
import com.app.maidi.infrastructures.AppComponent
import com.app.maidi.infrastructures.AppModules
import com.app.maidi.infrastructures.DaggerAppComponent
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager

class MainApplication : Application() {

    private var applicationComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        FlowManager.init(FlowConfig.builder(this).build())
        setApplicationComponent(
            DaggerAppComponent
            .builder()
            .appModules(AppModules(this))
            .build()
        )
    }

    fun getApplicationComponent() = applicationComponent

    fun setApplicationComponent(applicationComponent: AppComponent){
        this.applicationComponent = applicationComponent
    }
}
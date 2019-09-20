package com.app.maidi.infrastructures

import android.app.Application
import dagger.Module
import dagger.Provides

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
}
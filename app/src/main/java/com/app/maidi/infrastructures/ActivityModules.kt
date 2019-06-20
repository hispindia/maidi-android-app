package com.app.maidi.infrastructures

import android.app.Activity
import com.app.maidi.infrastructures.scope.ActivityScope
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ActivityModules {

    var activity: Activity

    constructor(activity: Activity){
        this.activity = activity
    }

    @Provides
    @ActivityScope
    fun provideActivity() : Activity{
        return activity
    }
}
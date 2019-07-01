package com.app.maidi.domains.main

import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.infrastructures.AppComponent
import com.app.maidi.infrastructures.scope.ActivityScope
import dagger.Component

@ActivityScope
@Component(modules = [ActivityModules::class], dependencies = [AppComponent::class])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
}
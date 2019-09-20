package com.app.maidi.infrastructures

import com.app.maidi.infrastructures.scope.ApplicationScope
import dagger.Component

@ApplicationScope
@Component(modules = [AppModules::class])
interface AppComponent {
}
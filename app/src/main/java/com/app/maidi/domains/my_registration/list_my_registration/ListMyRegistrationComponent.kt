package com.app.maidi.domains.my_registration.list_my_registration

import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.infrastructures.AppComponent
import com.app.maidi.infrastructures.scope.ActivityScope
import dagger.Component

@ActivityScope
@Component(modules = [ActivityModules::class], dependencies = [AppComponent::class])
interface ListMyRegistrationComponent {
    fun inject(listMyRegistrationActivity: ListMyRegistrationActivity)
}
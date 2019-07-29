package com.app.maidi.domains.child_registration

import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.infrastructures.AppComponent
import com.app.maidi.infrastructures.scope.ActivityScope
import dagger.Component

@ActivityScope
@Component(modules = [ActivityModules::class], dependencies = [AppComponent::class])
interface ChildRegistrationComponent {
    fun inject(childRegistrationActivity: ChildRegistrationActivity)
    fun inject(childRegistrationBeneficiaryActivity: ChildRegistrationBeneficiaryActivity)
}
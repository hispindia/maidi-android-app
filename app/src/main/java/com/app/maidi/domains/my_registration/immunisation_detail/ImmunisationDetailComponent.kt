package com.app.maidi.domains.my_registration.immunisation_detail

import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.infrastructures.AppComponent
import com.app.maidi.infrastructures.scope.ActivityScope
import dagger.Component

@ActivityScope
@Component(modules = [ActivityModules::class], dependencies = [AppComponent::class])
interface ImmunisationDetailComponent {
    fun inject(immunisationDetailActivity: ImmunisationDetailActivity)
}
package com.app.maidi.domains.my_registration.immunisation_detail

import android.os.Bundle
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.infrastructures.ActivityModules
import javax.inject.Inject

class ImmunisationDetailActivity : BaseActivity<ImmunisationDetailView, ImmunisationDetailPresenter>(){

    @Inject
    lateinit var immunisationDetailPresenter: ImmunisationDetailPresenter

    lateinit var mainApplication : MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout)
    }

    override fun createPresenter(): ImmunisationDetailPresenter {
        mainApplication = application as MainApplication
        DaggerImmunisationDetailComponent
            .builder()
            .appComponent(mainApplication.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)

        return immunisationDetailPresenter
    }
}
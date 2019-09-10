package com.app.maidi.domains.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.child_registration.ChildRegistrationActivity
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.fragments.aefi.RegisteredCasesFragment
import com.app.maidi.domains.main.fragments.immunisation.immunisation_card.ImmunisationChooseFragment
import com.app.maidi.domains.main.fragments.survey.ListSurveyFragment
import com.app.maidi.domains.main.fragments.workplan.MonthlyWorkplanChooseFragment
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program

class MainFragment : BaseFragment() {

    lateinit var mainActivity: MainActivity

    lateinit var currentProgram: Program

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var viewGroup = inflater.inflate(R.layout.fragment_main, container, false)
        ButterKnife.bind(this, viewGroup)
        mainActivity = activity as MainActivity

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.transparentActionBar()
        mainActivity.isSwipeForceSyncronizeEnabled(true)
    }

    @OnClick(R.id.activity_main_fl_child_registration)
    fun onChildRegistrationModuleClicked(){
        if(MetaDataController.getProgramByName(Constants.IMMUNISATION) != null) {
            var bundle = Bundle()
            bundle.putString(ChildRegistrationActivity.ORGANISATION_UNIT, Constants.IMMUNISATION)
            mainActivity.transformActivity(mainActivity, ChildRegistrationActivity::class.java, false, bundle)
        }
    }

    @OnClick(R.id.activity_main_fl_immunization_card)
    fun onMyImmunisationModuleClicked(){
        if(MetaDataController.getProgramByName(Constants.IMMUNISATION) != null) {
            mainActivity.transformFragment(
                R.id.activity_main_fl_content,
                ImmunisationChooseFragment()
            )
        }
    }

    @OnClick(R.id.activity_main_fl_aefi)
    fun onMyAefiModuleClicked(){
        if(MetaDataController.getProgramByName(Constants.AEFI) != null
            && MetaDataController.getProgramByName(Constants.IMMUNISATION) != null) {
            mainActivity.transformFragment(
                R.id.activity_main_fl_content,
                RegisteredCasesFragment()
            )
        }
    }

    @OnClick(R.id.activity_main_fl_survey)
    fun onSurveyModuleClicked(){
        if(MetaDataController.getProgramByName(Constants.SURVEY) != null) {
            mainActivity.transformFragment(
                R.id.activity_main_fl_content,
                ListSurveyFragment()
            )
        }
    }

    @OnClick(R.id.activity_main_fl_monthly_workplan)
    fun onMonthlyWorkplanModuleClicked(){
        if(MetaDataController.getProgramByName(Constants.WORKPLAN) != null) {
            mainActivity.transformFragment(
                R.id.activity_main_fl_content,
                MonthlyWorkplanChooseFragment()
            )
        }
    }
}
package com.app.maidi.domains.main.fragments.workplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage

class MonthlyWorkplanChooseFragment : BaseFragment(){

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_monthly_workplan_choose, container, false)
        ButterKnife.bind(this, viewGroup)

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    @OnClick(R.id.fragment_monthly_workplan_choose_cv_view)
    fun viewButtonClicked(){
        mainActivity.transformFragment(R.id.activity_main_fl_content, MonthlyWorkplanDetailFragment(false))
    }

    @OnClick(R.id.fragment_monthly_workplan_choose_cv_update)
    fun updateButtonClicked(){
        mainActivity.transformFragment(R.id.activity_main_fl_content, MonthlyWorkplanDetailFragment(true))
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
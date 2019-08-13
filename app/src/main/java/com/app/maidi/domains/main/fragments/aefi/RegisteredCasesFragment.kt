package com.app.maidi.domains.main.fragments.aefi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class RegisteredCasesFragment : BaseFragment(){

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program

    lateinit var adapter: RegisteredCaseAdapter

    @BindView(R.id.fragment_registered_cases_rcv_list)
    lateinit var rcvList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.AEFI)

        var viewGroup = inflater.inflate(R.layout.fragment_registered_cases, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvList.layoutManager = LinearLayoutManager(mainActivity)

        mainPresenter.getRemoteAefiTrackedEntityInstances(currentUnit.id, currentProgram.uid)

        return viewGroup
    }

    fun getRemoteTrackedEntityInstances(trackedEntityInstances: List<TrackedEntityInstance>){
        adapter = RegisteredCaseAdapter(mainActivity, trackedEntityInstances)
        rcvList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.registered_case))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
package com.app.maidi.domains.main.fragments.aefi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.aefi.AdverseEventInformationActivity
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class RegisteredCasesFragment : BaseFragment(), OnItemClickListener{

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program

    lateinit var adapter: RegisteredCaseAdapter
    lateinit var trackedEntityInstances: List<TrackedEntityInstance>

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

        mainPresenter.getAefiTrackedEntityInstances(currentUnit.id, currentProgram.uid)

        return viewGroup
    }

    fun getRemoteTrackedEntityInstances(trackedEntityInstances: List<TrackedEntityInstance>){
        this.trackedEntityInstances = trackedEntityInstances
        adapter = RegisteredCaseAdapter(mainActivity, this.trackedEntityInstances, this)
        rcvList.adapter = adapter
    }

    override fun onItemClicked(position: Int) {
        var trackedEntityInstance = trackedEntityInstances.get(position)
        var bundle = Bundle()
        bundle.putString(AdverseEventInformationActivity.TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstance.uid)
        mainActivity.transformActivity(mainActivity, AdverseEventInformationActivity::class.java, false, bundle)
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
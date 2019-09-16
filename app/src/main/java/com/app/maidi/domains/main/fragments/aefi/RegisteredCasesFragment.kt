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
import com.app.maidi.domains.child_registration.ChildRegistrationActivity
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.fragments.enrollment.EnrollmentDataEntryFragment
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment

class RegisteredCasesFragment : BaseFragment(), OnItemClickListener{

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var aefiProgram: Program
    lateinit var immunisationProgram: Program

    lateinit var adapter: RegisteredCaseAdapter
    lateinit var trackedEntityInstances: List<TrackedEntityInstance>

    @BindView(R.id.fragment_registered_cases_rcv_list)
    lateinit var rcvList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        aefiProgram = MetaDataController.getProgramByName(Constants.AEFI)
        immunisationProgram = MetaDataController.getProgramByName(Constants.IMMUNISATION)

        var viewGroup = inflater.inflate(R.layout.fragment_registered_cases, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvList.layoutManager = LinearLayoutManager(mainActivity)

        return viewGroup
    }

    fun getRemoteTrackedEntityInstances(trackedEntityInstances: List<TrackedEntityInstance>){
        this.trackedEntityInstances = trackedEntityInstances
        adapter = RegisteredCaseAdapter(mainActivity, this.trackedEntityInstances, this)
        rcvList.adapter = adapter
    }

    override fun onItemClicked(position: Int) {
        var trackedEntityInstance = trackedEntityInstances.get(position)
        var uniqueIdValue = TrackerController.getUniqueIdAttributeValue(trackedEntityInstance)
        var enrollment = TrackerController.getEnrollment(trackedEntityInstance)
        if(enrollment.program.uid.equals(aefiProgram.uid)){
            var bundle = Bundle()
            bundle.putString(AdverseEventInformationActivity.TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstance.uid)
            mainActivity.transformActivity(mainActivity, AdverseEventInformationActivity::class.java, false, bundle)
        }else{
            var bundle = Bundle()
            bundle.putString(ChildRegistrationActivity.PROGRAM, Constants.AEFI)
            if(uniqueIdValue != null){
                bundle.putString(ChildRegistrationActivity.UNIQUE_ID, uniqueIdValue.value)
            }
            mainActivity.transformActivity(mainActivity, ChildRegistrationActivity::class.java, false, bundle)
        }
    }

    override fun onResume() {
        super.onResume()

        var createButtonListener = View.OnClickListener {
            var bundle = Bundle()
            bundle.putString(ChildRegistrationActivity.PROGRAM, Constants.AEFI)
            mainActivity.transformActivity(mainActivity, ChildRegistrationActivity::class.java, false, bundle)
        }

        mainActivity.solidActionBar(resources.getString(R.string.registered_case), createButtonListener)
        mainActivity.isSwipeForceSyncronizeEnabled(false)
        mainPresenter.getAefiTrackedEntityInstances(currentUnit.id, aefiProgram.uid, immunisationProgram.uid)
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
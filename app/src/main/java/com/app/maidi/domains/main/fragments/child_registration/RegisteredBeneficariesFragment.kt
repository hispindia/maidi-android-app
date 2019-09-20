package com.app.maidi.domains.main.fragments.child_registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.child_registration.ChildRegistrationActivity
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.DhisService
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Enrollment
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import org.hisp.dhis.android.sdk.utils.UiUtils

class RegisteredBeneficariesFragment : BaseFragment(), OnItemClickListener{

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var orgUnit : OrganisationUnit
    lateinit var program : Program

    lateinit var adapter: RegisteredBeneficariesAdapter
    lateinit var dividerItemDecoration: DividerItemDecoration
    lateinit var trackedEntityInstances: List<TrackedEntityInstance>

    @BindView(R.id.fragment_registered_beneficaries_rcv_list)
    lateinit var rcvList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        orgUnit = MetaDataController.getTopAssignedOrganisationUnit()
        program = MetaDataController.getProgramByName(Constants.BENEFICIARY_CHILD_REGISTRATION)

        var viewGroup = inflater.inflate(R.layout.fragment_registered_beneficaries, container, false)
        ButterKnife.bind(this, viewGroup)

        dividerItemDecoration = DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.bg_divider))

        var layoutManager = object : LinearLayoutManager(mainActivity, VERTICAL, false){
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                mainActivity.hideLoading()
            }
        }
        rcvList.layoutManager = layoutManager
        rcvList.addItemDecoration(dividerItemDecoration)

        return viewGroup
    }

    fun getRegisteredTrackedEntityInstances(trackedEntityInstances: List<TrackedEntityInstance>){
        this.trackedEntityInstances = trackedEntityInstances
        adapter = RegisteredBeneficariesAdapter(mainActivity, this.trackedEntityInstances, this)
        rcvList.adapter = adapter
    }

    override fun onItemClicked(position: Int) {
        var trackedEntityInstance = trackedEntityInstances.get(position)
        var childNameValue = TrackerController.getTrackedEntityAttributeValueByDisplayName("Name", trackedEntityInstance)
        var dateOfBirthValue = TrackerController.getTrackedEntityAttributeValueByDisplayName("Date of Birth", trackedEntityInstance)
        var caregiverNameValue = TrackerController.getTrackedEntityAttributeValueByDisplayName("Caregiver name", trackedEntityInstance)
        var genderValue = TrackerController.getTrackedEntityAttributeValueByDisplayName("Gender", trackedEntityInstance)

        var childName: String? = null
        var dateOfBirth: String? = null
        var caregiverName: String? = null
        var gender:  String? = null

        if(childNameValue != null)
            childName = childNameValue.value

        if(dateOfBirthValue != null)
            dateOfBirth = dateOfBirthValue.value

        if(caregiverNameValue != null)
            caregiverName = caregiverNameValue.value

        if(genderValue != null)
            gender = genderValue.value

        UiUtils.showConfirmDialog(
            mainActivity, getString(R.string.warning),
            getString(R.string.enroll_child_into_immunisation_program),
            getString(R.string.enroll), getString(R.string.reject),
            { dialog, which ->
                var bundle = Bundle()
                bundle.putString(ChildRegistrationActivity.PROGRAM, Constants.IMMUNISATION)
                bundle.putBoolean(ChildRegistrationActivity.HAS_DATAS, true)
                bundle.putString(ChildRegistrationActivity.CHILD_NAME, childName)
                bundle.putString(ChildRegistrationActivity.GENDER, gender)
                bundle.putString(ChildRegistrationActivity.DATE_OF_BIRTH, dateOfBirth)
                bundle.putString(ChildRegistrationActivity.CAREGIVER, caregiverName)
                bundle.putString(ChildRegistrationActivity.CHILD_REGISTRATION_INSTANCE_ID, trackedEntityInstance.trackedEntityInstance)
                mainActivity.transformActivity(mainActivity, ChildRegistrationActivity::class.java, false, bundle)
            },
            { dialog, which ->
                val enrollment = TrackerController.getEnrollment(trackedEntityInstance)
                enrollment.setStatus(Enrollment.CANCELLED)
                enrollment.save()
                DhisService.updateTrackedEntityInstance(trackedEntityInstance)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.registered_beneficaries_title))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
        mainPresenter.getRegisteredBeneficariesInstances(orgUnit.id, program.uid)
    }

    fun createPresenter() : MainPresenter {
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
package com.app.maidi.domains.my_registration.list_my_registration

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.custom.MaidiCrashManagerListener
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.DaggerLoginComponent
import com.app.maidi.domains.my_registration.immunisation_detail.ImmunisationDetailActivity
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.utils.Constants
import net.hockeyapp.android.CrashManager
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.persistence.models.*
import javax.inject.Inject

class ListMyRegistrationActivity : BaseActivity<ListMyRegistrationView, ListMyRegistrationPresenter>(), ListMyRegistrationView, ListMyRegistrationAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{

    companion object{
        val TRACKED_ENTITY_INSTANCE = "TRACKED_INTITY_INSTANCE"
        val IS_FROM_SERVER = "IS_FROM_SERVER"
        val SEARCH_DATE = "SEARCH_DATE"
    }

    @Inject
    lateinit var listMyRegistrationPresenter: ListMyRegistrationPresenter

    lateinit var mainApplication: MainApplication

    @BindView(R.id.activity_list_my_registration_actionbar)
    lateinit var actionBar: View

    @BindView(R.id.activity_list_my_registration_rcv_list)
    lateinit var rcvList: RecyclerView

    @BindView(R.id.activity_list_my_registration_srl_refresh)
    lateinit var srlRefresh : SwipeRefreshLayout

    lateinit var birthDate: String
    lateinit var title: TextView
    lateinit var backButton : ImageView
    lateinit var adapter: ListMyRegistrationAdapter
    lateinit var dividerItemDecoration: DividerItemDecoration
    lateinit var trackedEntityInstances: List<TrackedEntityInstance>
    lateinit var orgUnit : OrganisationUnit
    lateinit var program :Program
    lateinit var userAccount : UserAccount
    lateinit var dateAttribute :TrackedEntityAttribute
    lateinit var phoneNumberAttribute : TrackedEntityAttribute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_my_registration)
        ButterKnife.bind(this)

        birthDate = intent.extras.getString(SEARCH_DATE)

        orgUnit = MetaDataController.getTopAssignedOrganisationUnit()
        program = MetaDataController.getProgramByName(Constants.IMMUNISATION)
        userAccount = MetaDataController.getUserAccount()
        phoneNumberAttribute = MetaDataController.getPhoneNumberAttribute()

        title = actionBar.findViewById(R.id.layout_actionbar_tv_title)
        title.text = birthDate

        backButton = actionBar.findViewById(R.id.layout_actionbar_iv_action)
        backButton.setOnClickListener {
            onBackPressed()
        }

        srlRefresh.setOnRefreshListener(this)

        dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.bg_divider))
        adapter = ListMyRegistrationAdapter(this, program.uid, arrayListOf(), this)
        rcvList.layoutManager = LinearLayoutManager(this)
        rcvList.addItemDecoration(dividerItemDecoration)
        rcvList.adapter = adapter

        queryListTrackedInstances()
    }

    fun queryListTrackedInstances(){
        var phoneAttributeValue = TrackedEntityAttributeValue()

        phoneAttributeValue.trackedEntityAttributeId = phoneNumberAttribute.uid
        phoneAttributeValue.value = userAccount.phoneNumber

        listMyRegistrationPresenter.queryListMyRegistration(orgUnit.id, program.uid, birthDate, phoneAttributeValue)
    }

    override fun onResume() {
        super.onResume()
        CrashManager.register(this, MaidiCrashManagerListener())
    }

    override fun onRefresh() {
        queryListTrackedInstances()
    }

    override fun createPresenter(): ListMyRegistrationPresenter {
        mainApplication = getApplication() as MainApplication
        DaggerListMyRegistrationComponent.builder()
            .appComponent(mainApplication.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return listMyRegistrationPresenter
    }

    override fun getListMyRegistrationSuccess(trackedEntityInstances: List<TrackedEntityInstance>) {
        runOnUiThread{
            this.trackedEntityInstances = trackedEntityInstances
            adapter = ListMyRegistrationAdapter(this, program.uid, this.trackedEntityInstances, this)
            rcvList.adapter = adapter
            if(srlRefresh != null && srlRefresh.isRefreshing)
                srlRefresh.isRefreshing = false
        }
    }

    override fun onItemClicked(position: Int) {
        var trackedEntityInstance = trackedEntityInstances.get(position)
        var bundle = Bundle()
        bundle.putSerializable(TRACKED_ENTITY_INSTANCE, trackedEntityInstance)
        if(!trackedEntityInstance.isFromServer)
            bundle.putBoolean(IS_FROM_SERVER, false)
        transformActivity(this, ImmunisationDetailActivity::class.java, false, bundle)
    }

    override fun getListMyRegistrationFailed(exception: APIException) {
        runOnUiThread {
            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun showLoading() {
        showHUD()
        updateText("Searching ...")
    }

    override fun hideLoading() {
        hideHUD()
    }
}
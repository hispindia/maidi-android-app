package com.app.maidi.domains.my_registration.immunisation_detail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.custom.MaidiCrashManagerListener
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.my_registration.list_my_registration.ListMyRegistrationActivity
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.models.Vaccine
import com.app.maidi.utils.Constants
import net.hockeyapp.android.CrashManager
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import javax.inject.Inject

class ImmunisationDetailActivity : BaseActivity<ImmunisationDetailView, ImmunisationDetailPresenter>(), ImmunisationDetailView{

    @Inject
    lateinit var immunisationDetailPresenter: ImmunisationDetailPresenter

    lateinit var mainApplication : MainApplication

    lateinit var trackedEntityInstance: TrackedEntityInstance

    lateinit var adapter: ImmunisationDetailAdapter

    lateinit var backButton : ImageView

    @BindView(R.id.activity_immunisation_detail_rcv_list)
    lateinit var rcvList: RecyclerView

    @BindView(R.id.activity_immunisation_detail_actionbar)
    lateinit var actionBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_immunisation_detail)
        ButterKnife.bind(this)

        trackedEntityInstance = intent.extras.
            getSerializable(ListMyRegistrationActivity.TRACKED_ENTITY_INSTANCE) as TrackedEntityInstance

        if(intent.extras.containsKey(ListMyRegistrationActivity.IS_FROM_SERVER))
            trackedEntityInstance.isFromServer = false

        backButton = actionBar.findViewById(R.id.layout_actionbar_iv_action)
        backButton.setOnClickListener {
            onBackPressed()
        }

        var organUnit = MetaDataController.getTopAssignedOrganisationUnit()
        var program = MetaDataController.getProgramByName(Constants.IMMUNISATION)

        rcvList.layoutManager = LinearLayoutManager(this)

        immunisationDetailPresenter.queryImmunisationInfo(organUnit.id, program.uid, trackedEntityInstance)

        /*if(trackedEntityInstance.isFromServer) {
            immunisationDetailPresenter.queryImmunisationInfo(organUnit.id, program.uid, trackedEntityInstance)
        }else{
            immunisationDetailPresenter.queryLocalImmunisationInfo(program.uid)
        }*/

        //immunisationDetailPresenter.queryLocalImmunisationInfo(program.uid)
    }

    override fun onResume() {
        super.onResume()
        CrashManager.register(this, MaidiCrashManagerListener())
    }

    override fun getDataElementSuccess(dataElements: ArrayList<Vaccine>) {
        runOnUiThread {
            adapter = ImmunisationDetailAdapter(this, dataElements)
            rcvList.adapter = adapter
            hideLoading()
        }
    }

    override fun getDataElementFailed(exception: APIException) {
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
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

    override fun showLoading() {
        showHUD()
    }

    override fun hideLoading() {
        hideHUD()
    }
}
package com.app.maidi.domains.child_registration

import android.os.Bundle
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.custom.MaidiCrashManagerListener
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.utils.DateUtils
import com.app.maidi.utils.MethodUtils.Companion.setupEditTextKeyboard
import com.squareup.otto.Subscribe
import net.hockeyapp.android.CrashManager
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent
import org.hisp.dhis.android.sdk.events.UiEvent
import org.hisp.dhis.android.sdk.fragments.enrollment.EnrollmentDataEntryFragment
import org.hisp.dhis.android.sdk.persistence.Dhis2Application
import org.hisp.dhis.android.sdk.persistence.models.*
import org.joda.time.DateTime
import javax.inject.Inject

class ChildRegistrationActivity : BaseActivity<ChildRegistrationView, ChildRegistrationPresenter>() {

    companion object{
        val ORGANISATION_UNIT = "ORGANISATION_UNIT"
    }

    @Inject
    lateinit var childPresenter: ChildRegistrationPresenter

    lateinit var application: MainApplication

    @BindView(R.id.activity_child_registration_ll_content)
    lateinit var llContent: LinearLayout

    @BindView(R.id.activity_child_registration_actionbar)
    lateinit var actionbar: RelativeLayout

    lateinit var trackedEntityInstance: TrackedEntityInstance

    lateinit var ivBack: ImageView
    lateinit var currentProgram: Program
    lateinit var topUnit: OrganisationUnit

    lateinit var enrollmentDataEntryFragment: EnrollmentDataEntryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_registration)
        ButterKnife.bind(this)

        setupEditTextKeyboard(llContent, this)

        topUnit = MetaDataController.getTopAssignedOrganisationUnit()
        if(intent.extras != null && intent.extras.getString(ORGANISATION_UNIT) != null) {
            currentProgram = MetaDataController.getProgramByName(intent.extras.getString(ORGANISATION_UNIT))
        }

        var enrollmentDate = DateUtils.convertCalendarToServerString(DateTime.now().toDate())
        var incidentDate = DateUtils.convertCalendarToServerString(DateTime.now().toDate())

        enrollmentDataEntryFragment = EnrollmentDataEntryFragment.newInstance(topUnit.id, currentProgram.uid, enrollmentDate, incidentDate)

        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_child_registration_container, enrollmentDataEntryFragment)
            .commit()

        ivBack = actionbar.findViewById(R.id.layout_actionbar_iv_action)
        ivBack.setOnClickListener({
            onBackPressed()
        })
    }

    @Subscribe
    fun updateUiEvent(uiEvent: UiEvent){
        when(uiEvent.eventType){
            UiEvent.UiEventType.START_SEND_DATA -> showHUD()
            UiEvent.UiEventType.ERROR_SEND_DATA -> hideHUD()
            UiEvent.UiEventType.SUCCESS_SEND_DATA -> {
                hideHUD()
                Toast.makeText(
                    this,
                    resources.getString(R.string.registration_successful),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    @Subscribe
    fun loadingEvent(loadingMessageEvent: LoadingMessageEvent){
        updateText(loadingMessageEvent.message)
        //Toast.makeText(this, loadingMessageEvent.message , Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Dhis2Application.bus.register(this)
        CrashManager.register(this, MaidiCrashManagerListener())
    }

    override fun onPause() {
        super.onPause()
        Dhis2Application.bus.unregister(this)
    }

    override fun onBackPressed() {
        enrollmentDataEntryFragment.showConfirmDiscardDialog()
        //super.onBackPressed()
    }

    override fun createPresenter(): ChildRegistrationPresenter {
        application = getApplication() as MainApplication
        DaggerChildRegistrationComponent.builder()
            .appComponent(application.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return childPresenter
    }

}
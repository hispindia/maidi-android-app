package com.app.maidi.domains.aefi

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.custom.MaidiCrashManagerListener
import com.app.maidi.utils.Constants
import com.app.maidi.utils.DateUtils
import com.app.maidi.utils.MethodUtils
import com.squareup.otto.Subscribe
import net.hockeyapp.android.CrashManager
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent
import org.hisp.dhis.android.sdk.events.UiEvent
import org.hisp.dhis.android.sdk.persistence.Dhis2Application
import org.hisp.dhis.android.sdk.persistence.models.*
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.lang.Exception

class AdverseEventInformationActivity : AppCompatActivity() {

    companion object{
        val TRACKED_ENTITY_INSTANCE_ID = "TRACKED_ENTITY_INSTANCE_ID"
    }

    @BindView(R.id.activity_adverse_event_information_ll_content)
    lateinit var llContent: LinearLayout

    @BindView(R.id.activity_adverse_event_information_actionbar)
    lateinit var actionbar: RelativeLayout

    lateinit var ivBack: ImageView
    lateinit var tvTitle: TextView
    lateinit var currentProgram: Program
    lateinit var topUnit: OrganisationUnit
    lateinit var programStage: ProgramStage
    lateinit var enrollment: Enrollment

    lateinit var trackedEntityInstanceId: String
    lateinit var eventDataEntryFragment: EventDataEntryFragment

    private var progressDialogLoading: Dialog? = null
    private var tvMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adverse_event_information)
        ButterKnife.bind(this)

        MethodUtils.setupEditTextKeyboard(llContent, this)

        try{
            if(intent.hasExtra(TRACKED_ENTITY_INSTANCE_ID)){
                trackedEntityInstanceId = intent.getStringExtra(TRACKED_ENTITY_INSTANCE_ID)
            }

            topUnit = MetaDataController.getTopAssignedOrganisationUnit()
            currentProgram = MetaDataController.getProgramByName(Constants.AEFI)
            programStage = MetaDataController.getProgramStageByName(currentProgram.uid, Constants.AEFI)
            enrollment = TrackerController.getEnrollmentByProgramAndTrackedEntityInstance(currentProgram.uid, trackedEntityInstanceId)

            eventDataEntryFragment = EventDataEntryFragment.newInstanceWithEnrollment(topUnit.id, currentProgram.uid, programStage.uid, enrollment.localId)

            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_adverse_event_information_container, eventDataEntryFragment)
                .commit()

            ivBack = actionbar.findViewById(R.id.layout_actionbar_iv_action)
            tvTitle = actionbar.findViewById(R.id.layout_actionbar_tv_title)
            tvTitle.setText(resources.getString(R.string.adverse_event_information))
            ivBack.setOnClickListener({
                onBackPressed()
            })
        }catch(ex : Exception){
            Log.e("Exception", ex.toString());
        }
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
                    resources.getString(R.string.create_update_successful),
                    Toast.LENGTH_LONG
                ).show()
                finish()
                //onBackPressed()
            }
        }
    }

    @Subscribe
    fun loadingEvent(loadingMessageEvent: LoadingMessageEvent){
        updateText(loadingMessageEvent.message)
    }

    override fun onResume() {
        super.onResume()
        CrashManager.register(this, MaidiCrashManagerListener())
        Dhis2Application.bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        Dhis2Application.bus.unregister(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun showHUD() {
        if (progressDialogLoading != null && progressDialogLoading!!.isShowing()) {
        } else {
            val view = layoutInflater.inflate(R.layout.layout_progress_loading_ball_spin, null)
            tvMessage = view.findViewById<TextView>(R.id.layout_loading_tv_message)
            progressDialogLoading = Dialog(this)
            progressDialogLoading!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialogLoading!!.setContentView(view)
            progressDialogLoading!!.setCancelable(false)
            progressDialogLoading!!.setCanceledOnTouchOutside(false)

            val window = progressDialogLoading!!.getWindow()
            if (window != null) {
                window!!.setBackgroundDrawableResource(R.drawable.bg_layout_loading)
            }
            progressDialogLoading!!.show()
        }
    }

    fun hideHUD() {
        if (progressDialogLoading != null && progressDialogLoading!!.isShowing()) {
            progressDialogLoading!!.dismiss()
        }
    }

    fun updateText(text: String){
        try {
            tvMessage!!.text = text
        }catch (exception : Exception){
            Log.d("Null Exception", exception.toString())
        }
    }

    override fun onBackPressed() {
        if (isCurrentFragment<EventDataEntryFragment>(R.id.activity_adverse_event_information_container)) {
            var handled = getCurrentFragment<EventDataEntryFragment>(R.id.activity_adverse_event_information_container).onBackPressed()
            if (handled)
                super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    inline fun <reified T> isCurrentFragment(containerId: Int) : Boolean {
        var fragment = supportFragmentManager.findFragmentById(containerId)
        return fragment is T
    }

    inline fun <reified T> getCurrentFragment(containerId: Int) : T{
        var fragment = supportFragmentManager.findFragmentById(containerId)
        return fragment as T
    }
}

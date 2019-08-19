package com.app.maidi.domains.aefi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.child_registration.ChildRegistrationActivity
import com.app.maidi.utils.Constants
import com.app.maidi.utils.Utils
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.*
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import org.joda.time.DateTime
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adverse_event_information)
        ButterKnife.bind(this)

        Utils.setupEditTextKeyboard(llContent, this)

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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onBackPressed() {
        eventDataEntryFragment.showConfirmDiscardDialog()
        //super.onBackPressed()
    }
}

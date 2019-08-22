package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.utils.Constants
import com.app.maidi.utils.LinearLayoutManagerWrapper
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.*
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment

class ImmunisationCardEventFragment: BaseFragment(), OnItemClickListener {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program
    lateinit var programStage: ProgramStage

    lateinit var events : List<Event>
    lateinit var trackedEntityInstanceId: String

    lateinit var adapter: ImmunisationCardEventAdapter

    @BindView(R.id.fragment_immunisation_card_event_rcv_list)
    lateinit var rcvList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_immunisation_card_event, container, false)
        ButterKnife.bind(this, viewGroup)

        arguments!!.let {
            if(it.containsKey("TRACKED_ENTITY_INSTANCE")){
                trackedEntityInstanceId = it.getString("TRACKED_ENTITY_INSTANCE")
                currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
                currentProgram = MetaDataController.getProgramByName(Constants.IMMUNISATION)
                programStage = TrackerController.getProgramStageByName(currentProgram.uid, Constants.IMMUNISATION)

                rcvList.layoutManager = LinearLayoutManagerWrapper(mainActivity, LinearLayoutManager.VERTICAL, false)

                events = TrackerController.getEventsThoughProgramStage(currentUnit.id, currentProgram.uid, programStage.uid, trackedEntityInstanceId)
                adapter = ImmunisationCardEventAdapter(mainActivity, events, this)
                rcvList.adapter = adapter
            }
        }

        return viewGroup
    }

    override fun onItemClicked(position: Int) {
        var event = events.get(position)
        var enrollment = TrackerController.getEnrollment(event.enrollment)
        var eventDataEntryFragment
                = EventDataEntryFragment.newInstanceWithEnrollment(currentUnit.id, currentProgram.uid, programStage.uid, enrollment.localId, event.localId)
        mainActivity.transformFragment(R.id.activity_main_fl_content, eventDataEntryFragment)
        mainActivity.solidActionBar(resources.getString(R.string.immunisation_card_update_title))
    }

    override fun onResume() {
        super.onResume()

        var createButtonListener = View.OnClickListener {
            var enrollment = TrackerController.getEnrollmentByProgramAndTrackedEntityInstance(currentProgram.uid, trackedEntityInstanceId)
            mainActivity.transformFragment(R.id.activity_main_fl_content,
                EventDataEntryFragment.newInstanceWithEnrollment(currentUnit.id, currentProgram.uid, programStage.uid, enrollment.localId))
            mainActivity.solidActionBar(resources.getString(R.string.immunisation_card_update_title))
        }

        mainActivity.solidActionBar(resources.getString(R.string.immunisation_card_update_title), createButtonListener)
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    fun createPresenter() : MainPresenter {
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
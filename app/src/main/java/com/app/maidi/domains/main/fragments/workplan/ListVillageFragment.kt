package com.app.maidi.domains.main.fragments.workplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.domains.main.fragments.listener.OnSurveyItemClickListener
import com.app.maidi.domains.main.fragments.survey.ListSurveyAdapter
import com.app.maidi.utils.Constants
import com.app.maidi.utils.LinearLayoutManagerWrapper
import com.app.maidi.utils.Utils
import com.app.maidi.widget.ExportPDF
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import org.joda.time.DateTime

class ListVillageFragment : BaseFragment, OnItemClickListener {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program
    lateinit var programStage: ProgramStage

    lateinit var adapter: ListVillageAdapter

    var eventMaps = hashMapOf<Long, String>()
    var eventList: List<Event>
    var eventDate: String

    @BindView(R.id.fragment_list_survey_rcv_list)
    lateinit var rcvList: RecyclerView

    constructor(eventDate: String, eventList: List<Event>){
        this.eventDate = eventDate
        this.eventList = eventList
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.WORKPLAN)
        programStage = MetaDataController.getProgramStageByName(currentProgram.uid, Constants.WORKPLAN)

        var viewGroup = inflater.inflate(R.layout.fragment_list_workplan, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvList.layoutManager = LinearLayoutManagerWrapper(mainActivity, LinearLayoutManager.VERTICAL, false)

        getVillageListByDate()
        adapter = ListVillageAdapter(mainActivity, eventMaps, this)
        rcvList.adapter = adapter

        return viewGroup
    }

    override fun onItemClicked(position: Int) {
        var eventId = eventMaps.keys.toList().get(position)
        mainActivity.transformFragment(R.id.activity_main_fl_content,
            EventDataEntryFragment.newWorkplanEventInstance(currentUnit.id, currentProgram.uid, programStage.uid, eventId))
        mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan_update))
    }

    fun getVillageListByDate(){
        for(event in eventList){
            var date: String?
            if(Utils.isValidDateFollowPattern(event.eventDate))
                date = Utils.convertServerDateToLocalDate(event.eventDate)
            else
                date = Utils.convertFromFullDateToSimpleDate(event.eventDate)

            if(date.equals(eventDate)){
                var values = TrackerController.getDataValue(event.event)
                for(value in values){
                    var organUnit = MetaDataController.getOrganisationUnitById(value.value)
                    if(organUnit != null) {
                        eventMaps.put(event.localId, organUnit.displayName)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan_update))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    fun createPresenter() : MainPresenter {
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
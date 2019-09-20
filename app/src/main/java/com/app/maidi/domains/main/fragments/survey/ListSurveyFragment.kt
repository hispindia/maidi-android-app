package com.app.maidi.domains.main.fragments.survey

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
import com.app.maidi.domains.main.fragments.listener.OnSurveyItemClickListener
import com.app.maidi.utils.Constants
import com.app.maidi.utils.DateUtils
import com.app.maidi.utils.LinearLayoutManagerWrapper
import com.app.maidi.widget.ExportPDF
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import org.joda.time.DateTime

class ListSurveyFragment : BaseFragment(), OnSurveyItemClickListener{

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program
    lateinit var programStage: ProgramStage

    lateinit var adapter: ListSurveyAdapter
    var exportEventId: Long = -1L

    @BindView(R.id.fragment_list_survey_rcv_list)
    lateinit var rcvList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.SURVEY)
        programStage = MetaDataController.getProgramStageByName(currentProgram.uid, Constants.SURVEY)

        var viewGroup = inflater.inflate(R.layout.fragment_list_survey, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvList.layoutManager = LinearLayoutManagerWrapper(mainActivity, LinearLayoutManager.VERTICAL, false)

        return viewGroup
    }

    override fun onViewButtonClicked(localEventId: Long) {
        mainActivity.transformFragment(R.id.activity_main_fl_content,
            EventDataEntryFragment.newSurveyEventInstance(currentUnit.id, currentProgram.uid, programStage.uid, localEventId))
        mainActivity.solidActionBar(resources.getString(R.string.house_to_house_survey))
    }

    override fun onExportButtonClicked(localEventId: Long) {
        exportEventId = localEventId
        mainActivity.checkStoragePermissions()
    }

    override fun onResume() {
        super.onResume()

        var createButtonListener = View.OnClickListener {
            mainActivity.transformFragment(R.id.activity_main_fl_content,
                EventDataEntryFragment.newEventInstance(currentUnit.id, currentProgram.uid, programStage.uid))
            mainActivity.solidActionBar(resources.getString(R.string.house_to_house_survey))
        }

        mainActivity.solidActionBar(resources.getString(R.string.surveys), createButtonListener)
        mainActivity.isSwipeForceSyncronizeEnabled(false)
        mainPresenter.getSurveyEntities(currentUnit.id, currentProgram.uid)
    }

    fun getEventsListSuccess(events: List<Event>){
        adapter = ListSurveyAdapter(mainActivity, events, this)
        rcvList.adapter = adapter
    }

    fun exportSurveyToPdf(){
        try{
            var pdf = ExportPDF(mainActivity)
            var reportDate = DateTime.now().millis
            var pdfFile = pdf.openLandscapeDocument("Report_MAIDI_Survey_" + reportDate + ".pdf")
            var event = TrackerController.getEvent(exportEventId)
            var sections = TrackerController.getProgramStageSections(programStage.uid)
            var eventDate = ""

            pdf.addTitle("Mobile Application for Immunisation Datas of India",
                "Survey Form",
                DateUtils.simpleLocalDateFormat.format(DateTime.now().toDate())
            )

            event!!.let {
                if(DateUtils.isValidDateFollowPattern(it.eventDate))
                    eventDate = DateUtils.convertServerDateToLocalDate(it.eventDate)
                else
                    eventDate = DateUtils.convertFromFullDateToSimpleDate(it.eventDate)
            }

            pdf.addRightParagraph("Survey date: " + eventDate)

            sections!!.let {
                for(section in it){
                    // Add section name
                    pdf.addParagraph(section.displayName)
                    pdf.createForm(event, section.programStageDataElements)
                }
            }

            pdf.closeDocument()
            mainActivity.openExportFolderDialog(pdfFile)
        }catch (ex: Exception){
            Toast.makeText(mainActivity, resources.getString(R.string.export_pdf_file_failed), Toast.LENGTH_LONG).show()
        }
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.models.Vaccine
import com.app.maidi.utils.Constants
import com.app.maidi.utils.LinearLayoutManagerWrapper
import com.app.maidi.utils.DateUtils
import com.app.maidi.widget.ExportPDF
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.joda.time.DateTime
import java.lang.StringBuilder

class ImmunisationCardFragment : BaseFragment() {

    val exportHeaders = arrayOf("#", "Vaccine's name", "Due Date")

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program

    lateinit var adapter: ImmunisationCardAdapter
    lateinit var immunisationList: List<ImmunisationCard>

    @BindView(R.id.fragment_immunisation_card_rcv_list)
    lateinit var rcvList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.IMMUNISATION)

        var viewGroup = inflater.inflate(R.layout.fragment_immunisation_card, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvList.layoutManager = LinearLayoutManagerWrapper(mainActivity, LinearLayoutManager.VERTICAL, false)

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.immunisation_card_title))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
        mainPresenter.getImmunisationTrackedEntityInstances(currentUnit.id, currentProgram.uid)
    }

    @OnClick(R.id.fragment_immunisation_card_cv_update)
    fun onUpdateButtonClicked(){
        transformToEventListFragment()
    }

    @OnClick(R.id.fragment_immunisation_card_cv_download)
    fun onDownloadButtonClicked(){
        mainActivity.checkStoragePermissions()
    }

    fun transformToEventListFragment(){
        if(adapter.selectPosition > -1){
            var immunisationCard = adapter.getItem(adapter.selectPosition)
            immunisationCard!!.let {
                var trackedEntityInstance = it.trackedEntityInstance
                var args = Bundle()
                args.putString("TRACKED_ENTITY_INSTANCE", trackedEntityInstance.uid)
                var immunisationCardEventFragment = ImmunisationCardEventFragment()
                immunisationCardEventFragment.arguments = args
                mainActivity.transformFragment(R.id.activity_main_fl_content,
                    immunisationCardEventFragment
                )
            }
        }
    }

    fun updateImmunisationCardList(immunisationList: List<ImmunisationCard>){
        this.immunisationList = immunisationList
        adapter = ImmunisationCardAdapter(mainActivity, immunisationList)
        rcvList.adapter = adapter
    }

    fun exportDatasToPdf(){
        try{
            var pdf = ExportPDF(mainActivity)
            var reportDate = DateTime.now().millis
            var pdfFile = pdf.openDocument("Report_MAIDI_ImmunisationCards_" + reportDate + ".pdf")
            pdf.addTitle("Mobile Application for Immunisation Datas of India",
                "Immunisation Records",
                DateUtils.simpleLocalDateFormat.format(DateTime.now().toDate())
            )

            for(item in immunisationList){
                var builder = StringBuilder()

                if(item.trackedEntityInstance != null && item.trackedEntityInstance.attributes != null) {
                    var attributes = item.trackedEntityInstance.attributes
                    for(attribute in attributes){
                        if(attribute.displayName.contains("Name")){
                            builder.append("Name of child: " + attribute.value + ", ")
                            break
                        }
                    }
                }

                item.enrollment!!.let {
                    if(DateUtils.isValidDateFollowPattern(it.incidentDate))
                        builder.append("Date of Birth: " + DateUtils.convertServerDateToLocalDate(it.incidentDate) + ", ")
                    else
                        builder.append("Date of Birth: " + DateUtils.convertFromFullDateToSimpleDate(it.incidentDate) + ", ")
                }

                builder.append("Reg ID: " + item.trackedEntityInstance.uid)
                pdf.addParagraph(builder.toString())
                pdf.createTable(exportHeaders, vaccineList(item.vaccineList))
            }

            pdf.closeDocument()
            mainActivity.openExportFolderDialog(pdfFile)
            //Toast.makeText(mainActivity, resources.getString(R.string.export_pdf_file_success), Toast.LENGTH_LONG).show()
        }catch (ex: Exception){
            Toast.makeText(mainActivity, resources.getString(R.string.export_pdf_file_failed), Toast.LENGTH_LONG).show()
        }
    }

    fun vaccineList(vaccines : List<Vaccine>) : ArrayList<Array<String>>{
        var vaccineList = arrayListOf<Array<String>>()
        var counter = 1
        for(vaccine in vaccines){
            var vaccineName = vaccine.dataElement.displayName
            var vaccineDueDate = ""
            if(vaccine.dueDate != null && !vaccine.dueDate.isEmpty()){
                if(DateUtils.isValidDateFollowPattern(vaccine.dueDate))
                    vaccineDueDate = DateUtils.convertServerDateToLocalDate(vaccine.dueDate)
                else
                    vaccineDueDate = DateUtils.convertFromFullDateToSimpleDate(vaccine.dueDate)
            }
            vaccineList.add(arrayOf(counter.toString(), vaccineName, vaccineDueDate))
            counter++
        }

        return vaccineList
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
package com.app.maidi.domains.main.fragments.immunisation.session_wise

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.immunisation.immunisation_card.ImmunisationCardAdapter
import com.app.maidi.models.Dose
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program

class SessionWiseDataListFragment : BaseFragment(){

    companion object{
        val SESSION_DATE = "SEARCH_DATE"
    }

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var dataElements : List<DataElement>

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program
    lateinit var adapter: SessionWiseDataAdapter

    @BindView(R.id.fragment_session_wise_data_list_ll_vaccine)
    lateinit var llVaccine: LinearLayout

    @BindView(R.id.fragment_session_wise_data_list_ll_total_dose)
    lateinit var llTotalDose: LinearLayout

    @BindView(R.id.fragment_session_wise_data_list_rcv_child_list)
    lateinit var rcvChildList: RecyclerView

    lateinit var sessionDate: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        arguments!!.let {
            if(it.containsKey(SESSION_DATE)){
                sessionDate = it.getString(SESSION_DATE)
            }
        }

        //mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.IMMUNISATION)

        var viewGroup = inflater.inflate(R.layout.fragment_session_wise_data_list, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvChildList.layoutManager = LinearLayoutManager(mainActivity)

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainPresenter.getSessionWiseDatas(currentUnit.id, currentProgram.uid, sessionDate)
    }

    fun getProgramDataElements(dataElements : List<DataElement>){
        this.dataElements = dataElements
        for(dataElement in dataElements){
            var itemView = LayoutInflater.from(context).inflate(R.layout.item_session_vaccine, null)
            var tvVaccineName = itemView.findViewById<TextView>(R.id.item_session_vaccine_tv_vaccine_name)
            tvVaccineName.text = dataElement.displayName
            llVaccine.addView(itemView)
        }
    }

    fun getTotalDoses(doseList: List<Dose>){
        for(element in dataElements){

            var itemView = LayoutInflater.from(context).inflate(R.layout.item_session_vaccine, null)
            var tvVaccineName = itemView.findViewById<TextView>(R.id.item_session_vaccine_tv_vaccine_name)
            var tvVaccineDose = itemView.findViewById<TextView>(R.id.item_session_vaccine_tv_vaccine_dose)

            tvVaccineName.visibility = View.INVISIBLE
            tvVaccineDose.visibility = View.VISIBLE
            tvVaccineName.text = element.displayName
            tvVaccineDose.text = "0"
            for(dose in doseList){
                if(dose.elementId.equals(element.uid)){
                    tvVaccineDose.text = if(dose.dose == 0) "0" else String.format("%2d", dose.dose)
                    break
                }
            }

            llTotalDose.addView(itemView)
        }
    }

    fun getSessionWiseDataList(sessionWiseList: List<ImmunisationCard>){
        adapter = SessionWiseDataAdapter(mainActivity, sessionWiseList)
        rcvChildList.adapter = adapter
        mainActivity.hideHUD()
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
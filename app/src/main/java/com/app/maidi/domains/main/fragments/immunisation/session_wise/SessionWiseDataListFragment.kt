package com.app.maidi.domains.main.fragments.immunisation.session_wise

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.utils.Constants
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program

class SessionWiseDataListFragment : BaseFragment(){

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program

    lateinit var dataElements: List<DataElement>
    lateinit var adapter: ImmunisationCardAdapter

    @BindView(R.id.fragment_session_wise_data_list_ll_vaccine)
    lateinit var llVaccine: LinearLayout

    @BindView(R.id.fragment_session_wise_data_list_rcv_child_list)
    lateinit var rcvChildList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        //mainActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.IMMUNISATION)

        var viewGroup = inflater.inflate(R.layout.fragment_session_wise_data_list, container, false)
        ButterKnife.bind(this, viewGroup)

        rcvChildList.layoutManager = LinearLayoutManager(mainActivity)

        dataElements = mainPresenter.getProgramDataElement(currentProgram.uid)

        for(dataElement in dataElements){
            mainActivity.showHUD()
            var tvElement = TextView(mainActivity)
            tvElement.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
            tvElement.isAllCaps = true
            tvElement.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.4f)
            tvElement.setTextColor(mainActivity.resources.getColor(R.color.dark_gray))
            tvElement.gravity = Gravity.CENTER_VERTICAL
            tvElement.setPadding(
                resources.getDimensionPixelSize(R.dimen.text_padding), 0,
                resources.getDimensionPixelSize(R.dimen.text_padding), 0)
            tvElement.text = dataElement.displayName
            llVaccine.addView(tvElement)
            mainActivity.hideHUD()
        }

        mainPresenter.getSessionWiseDataList(currentUnit.id, currentProgram.uid)

        return viewGroup
    }

    fun getSessionWiseDataList(sessionWiseList: List<ImmunisationCard>){
        adapter = ImmunisationCardAdapter(mainActivity, sessionWiseList)
        rcvChildList.adapter = adapter
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
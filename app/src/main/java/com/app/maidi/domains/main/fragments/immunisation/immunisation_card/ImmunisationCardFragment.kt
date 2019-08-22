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
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.utils.Constants
import com.app.maidi.utils.LinearLayoutManagerWrapper
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Constant
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program

class ImmunisationCardFragment : BaseFragment() {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program

    lateinit var adapter: ImmunisationCardAdapter

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

    @OnClick(R.id.fragment_immunisation_card_cv_update)
    fun onUpdateButtonClicked(){
        transformToEventListFragment()
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
        adapter = ImmunisationCardAdapter(mainActivity, immunisationList)
        rcvList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.immunisation_card_title))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
        mainPresenter.getRemoteTrackedEntityInstances(currentUnit.id, currentProgram.uid)
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
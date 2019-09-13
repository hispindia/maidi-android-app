package com.app.maidi.domains.main.fragments.workplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.utils.Constants
import com.app.maidi.utils.DateUtils
import kotlinx.android.synthetic.main.item_workplan_day_of_week.view.*
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import org.joda.time.LocalDate
import java.lang.StringBuilder

class WeeklyWorkplanFragment : BaseFragment{

    val days_of_week = arrayOf<Int>(R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday, R.string.friday, R.string.saturday)

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program
    lateinit var programStage: ProgramStage

    private var dayList: ArrayList<LocalDate>
    private var workplanList: List<Event>
    private var isEditMode = false

    @BindView(R.id.fragment_weekly_workplan_ll_container)
    lateinit var llContainer: LinearLayout

    constructor(dayList: ArrayList<LocalDate>, workplanList: List<Event>, isEditMode : Boolean){
        this.dayList = dayList
        this.workplanList = workplanList
        this.isEditMode = isEditMode
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.WORKPLAN)
        programStage = MetaDataController.getProgramStageByName(currentProgram.uid, Constants.WORKPLAN)

        var viewGroup = inflater.inflate(R.layout.fragment_weekly_workplan, container, false)
        ButterKnife.bind(this, viewGroup)

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        initDayViews()
    }

    fun initDayViews(){
        for(i in 0 .. 5) {
            var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, resources.getDimensionPixelSize(R.dimen.item_height))
            var itemView = LayoutInflater.from(mainActivity).inflate(R.layout.item_workplan_day_of_week, null, false)
            var llContent = itemView.item_workplan_day_of_week_ll_content
            var tvDayName = itemView.item_workplan_day_of_week_tv_day_name
            var tvPlanDate = itemView.item_workplan_day_of_week_tv_plan_date
            var tvVillages = itemView.item_workplan_day_of_week_tv_village
            if(i % 2 == 0)
                llContent.setBackgroundColor(resources.getColor(R.color.lighter_gray_background_color))
            else
                llContent.setBackgroundColor(resources.getColor(android.R.color.white))
            tvDayName.setText(resources.getString(days_of_week[i]))
            for(day in dayList){
                if(day.dayOfWeek == i + 1){
                    var date = DateUtils.convertCalendarToString(day.toDate())
                    tvPlanDate.text = date
                    var builder = StringBuilder()
                    for(event in workplanList){
                        var eventDate: String?
                        if(event.eventDate != null) {
                            if (DateUtils.isValidDateFollowPattern(event.eventDate))
                                eventDate = DateUtils.convertServerDateToLocalDate(event.eventDate)
                            else
                                eventDate = DateUtils.convertFromFullDateToSimpleDate(event.eventDate)

                            if (eventDate.equals(date)) {
                                var values = TrackerController.getDataValue(event.event)
                                for (value in values) {
                                    var organUnit = MetaDataController.getOrganisationUnitById(value.value)
                                    if (organUnit != null) {
                                        builder.append(organUnit!!.displayName + " ")
                                    }
                                }
                            }
                        }
                    }
                    tvVillages.text = builder.toString().trim()
                    if(isEditMode){
                        llContent.setOnClickListener(
                            object : View.OnClickListener{
                                override fun onClick(p0: View?) {
                                    if(workplanList != null && workplanList.size > 0) {
                                        if(checkDateHasWorkplan(tvPlanDate.text.toString())){
                                            mainActivity.transformFragment(
                                                R.id.activity_main_fl_content,
                                                ListVillageFragment(tvPlanDate.text.toString())
                                            )
                                            return
                                        }
                                    }

                                    mainActivity.transformFragment(R.id.activity_main_fl_content,
                                        EventDataEntryFragment.newWorkplanEventInstanceWithSpecificDate(
                                            currentUnit.id, currentProgram.uid, programStage.uid,
                                            DateUtils.convertLocalDateToServerDate(tvPlanDate.text.toString())))
                                    mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan_create_new_event))
                                }
                        })
                    }
                    break
                }
            }

            itemView.layoutParams = params
            llContainer.addView(itemView)
        }
    }

    override fun onStop() {
        super.onStop()
        if(llContainer != null) {
            llContainer.removeAllViews()
        }
    }

    fun createPresenter() : MainPresenter {
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }

    fun setDayList(dayList: ArrayList<LocalDate>){
        this.dayList = dayList
    }

    fun checkDateHasWorkplan(chooseDate: String) : Boolean{
        if(workplanList != null) {
            for (event in workplanList) {
                var date: String?
                if (DateUtils.isValidDateFollowPattern(event.eventDate))
                    date = DateUtils.convertServerDateToLocalDate(event.eventDate)
                else
                    date = DateUtils.convertFromFullDateToSimpleDate(event.eventDate)

                if (date.equals(chooseDate)) {
                    return true
                }
            }
        }

        return false
    }
}
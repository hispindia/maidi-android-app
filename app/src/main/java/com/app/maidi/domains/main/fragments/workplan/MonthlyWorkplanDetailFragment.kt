package com.app.maidi.domains.main.fragments.workplan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.BuildConfig
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.utils.Constants
import com.app.maidi.utils.Utils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.whiteelephant.monthpicker.MonthPickerDialog
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MonthlyWorkplanDetailFragment : BaseFragment, MonthPickerDialog.OnDateSetListener{

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    lateinit var currentUnit: OrganisationUnit
    lateinit var currentProgram: Program
    lateinit var programStage: ProgramStage

    lateinit var weeklyPagerAdapter: WeeklyPagerAdapter

    var weekList = HashMap<String, ArrayList<LocalDate>>()
    var workplanList = listOf<Event>()
    private var isEditMode = false

    @BindView(R.id.fragment_monthly_workplan_vp_pagers)
    lateinit var vpPagers : ViewPager

    @BindView(R.id.fragment_monthly_workplan_tbl_tabs)
    lateinit var tblTabs: TabLayout

    @BindView(R.id.fragment_monthly_workplan_detail_et_selected_month)
    lateinit var etSelectedMonth: TextInputEditText

    constructor(isEditMode : Boolean){
        this.isEditMode = isEditMode
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainActivity = activity as MainActivity
        createPresenter()

        currentUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.WORKPLAN)
        programStage = MetaDataController.getProgramStageByName(currentProgram.uid, Constants.WORKPLAN)

        var viewGroup = inflater.inflate(R.layout.fragment_monthly_workplan_detail, container, false)
        ButterKnife.bind(this, viewGroup)

        etSelectedMonth.setText(Utils.convertCalendarToMonthString(Calendar.getInstance().time))
        weeklyPagerAdapter = WeeklyPagerAdapter(childFragmentManager!!, weekList, workplanList, isEditMode)
        vpPagers.adapter = weeklyPagerAdapter
        //vpPagers.offscreenPageLimit = weekList.size
        tblTabs.setupWithViewPager(vpPagers)

        return viewGroup
    }

    override fun onResume() {
        super.onResume()

        var createButtonListener = View.OnClickListener {
            mainActivity.transformFragment(R.id.activity_main_fl_content,
                EventDataEntryFragment.newWorkplanEventInstance(currentUnit.id, currentProgram.uid, programStage.uid))
            mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan_create_new_event))
        }

        if(isEditMode){
            mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan_update), createButtonListener)
        }else{
            mainActivity.solidActionBar(resources.getString(R.string.monthly_workplan), createButtonListener)
        }

        mainActivity.isSwipeForceSyncronizeEnabled(false)
        mainPresenter.getWorkplanEntities(currentUnit.id, currentProgram.uid)
    }

    override fun onDateSet(selectedMonth: Int, selectedYear: Int) {
        var month = if(selectedMonth < 9) "0" + (selectedMonth + 1) else (selectedMonth + 1).toString()
        var monthDate = month + "/" + selectedYear.toString()
        initWeekPager(monthDate)
    }

    @OnClick(R.id.fragment_monthly_workplan_detail_v_selected_month)
    fun openMonthChooser(){
        var monthDate = Utils.convertMonthStringToCalendar(etSelectedMonth.text.toString())
        mainActivity.showSelectMonthChooseDialog(this, monthDate)
    }

    fun getWorkplanList(events : List<Event>){
        this.workplanList = events
        initWeekPager(etSelectedMonth.text.toString())
    }

    fun initWeekPager(monthDate: String){
        etSelectedMonth.setText(monthDate)
        getWeekList(monthDate)
        weeklyPagerAdapter.setData(weekList, workplanList)
    }

    fun getWeekList(monthDate: String){
        weekList = hashMapOf()
        var firstDay = Utils.convertMonthStringToLocalDate(monthDate).withDayOfMonth(1)
        var checkDay = firstDay
        var currentWeek = 1
        var weekName = "Week " + currentWeek
        var daysPerWeek = arrayListOf<LocalDate>()
        while(firstDay.monthOfYear == checkDay.monthOfYear){
            when(checkDay.dayOfWeek){
                DateTimeConstants.MONDAY,
                DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY,
                DateTimeConstants.THURSDAY,
                DateTimeConstants.FRIDAY,
                DateTimeConstants.SATURDAY -> {
                    daysPerWeek.add(checkDay)
                }
                DateTimeConstants.SUNDAY -> {
                    daysPerWeek.add(checkDay)
                    weekList.put(weekName, daysPerWeek)
                    daysPerWeek = arrayListOf()
                    currentWeek++
                    weekName = "Week " + currentWeek
                }
            }
            checkDay = checkDay.plusDays(1)
        }
        if(daysPerWeek.size > 0){
            weekList.put(weekName, daysPerWeek)
        }
    }

    fun createPresenter() : MainPresenter {
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
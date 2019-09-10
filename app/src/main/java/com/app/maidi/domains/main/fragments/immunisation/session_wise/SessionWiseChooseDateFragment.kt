package com.app.maidi.domains.main.fragments.immunisation.session_wise

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.utils.DateUtils
import com.google.android.material.textfield.TextInputEditText
import org.joda.time.DateTime
import org.joda.time.LocalDate

class SessionWiseChooseDateFragment : BaseFragment(), DatePickerDialog.OnDateSetListener/*, SingleDateAndTimePicker.OnDateChangedListener*/ {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    @BindView(R.id.fragment_session_wise_choose_date_et_date_of_birth)
    lateinit var etDateOfBirth: TextInputEditText

    /*@BindView(R.id.fragment_session_wise_choose_date_picker)
    lateinit var datePicker: SingleDateAndTimePicker*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_session_wise_choose_date, container, false)
        ButterKnife.bind(this, viewGroup)

        //datePicker.addOnDateChangedListener(this)
        etDateOfBirth.setText(DateUtils.convertCalendarToDayOfWeekString(DateTime.now().toDate()))

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.session_wise_vaccination_due))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
        //datePicker.selectDate(DateUtils.convertDayWeekStringToCalendar(etDateOfBirth.text.toString()))
    }

    @OnClick(R.id.fragment_session_wise_choose_date_v_date_of_birth)
    fun onDateChooseClicked(){
        var chooseDate = DateUtils.convertDayWeekStringToCalendar(etDateOfBirth.text.toString())
        var dateDialog = DatePickerDialog(context, this, chooseDate.getYear(), chooseDate.getMonthOfYear() - 1, chooseDate.getDayOfMonth())
        dateDialog.show()
    }

    override fun onDateSet(view : DatePicker, year: Int, monthOfYear : Int, dayOfMonth: Int) {
        val date = LocalDate(year, monthOfYear + 1, dayOfMonth)
        etDateOfBirth.setText(DateUtils.convertCalendarToDayOfWeekString(date.toDate()))
    }

    /*override fun onDateChanged(displayed: String?, date: Date?) {
        date!!.let {
            etDateOfBirth.setText(DateUtils.convertCalendarToDayOfWeekString(date))
        }
    }*/

    @OnClick(R.id.fragment_session_wise_choose_date_btn_ok)
    fun onOkButtonClicked(){
        mainActivity.showHUD()
        Handler().postDelayed({
            gotoSessionWiseDataFragment()
            mainActivity.hideHUD()
        }, 500)
    }

    fun gotoSessionWiseDataFragment(){
        var bundle = Bundle()
        bundle.putString(SessionWiseDataListFragment.SESSION_DATE, DateUtils.convertDayOfWeekDateToServerDate(etDateOfBirth.text.toString()))
        var sessionWiseDataListFragment = SessionWiseDataListFragment()
        sessionWiseDataListFragment.arguments = bundle
        mainActivity.transformFragment(R.id.activity_main_fl_content, sessionWiseDataListFragment)
    }

    @OnClick(R.id.fragment_session_wise_choose_date_btn_cancel)
    fun onCancelButtonClicked(){
        mainActivity.onBackPressed()
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
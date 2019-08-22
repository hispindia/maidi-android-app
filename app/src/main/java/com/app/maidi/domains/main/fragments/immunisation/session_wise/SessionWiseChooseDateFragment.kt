package com.app.maidi.domains.main.fragments.immunisation.session_wise

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.my_registration.list_my_registration.ListMyRegistrationActivity
import com.app.maidi.utils.Utils
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.textfield.TextInputEditText
import org.joda.time.DateTime
import java.util.*

class SessionWiseChooseDateFragment : BaseFragment(), SingleDateAndTimePicker.OnDateChangedListener {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    @BindView(R.id.fragment_session_wise_choose_date_et_date_of_birth)
    lateinit var etDateOfBirth: TextInputEditText

    @BindView(R.id.fragment_session_wise_choose_date_picker)
    lateinit var datePicker: SingleDateAndTimePicker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_session_wise_choose_date, container, false)
        ButterKnife.bind(this, viewGroup)

        datePicker.addOnDateChangedListener(this)
        etDateOfBirth.setText(Utils.convertCalendarToString(DateTime.now().toDate()))

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.session_wise_vaccination_due))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    override fun onDateChanged(displayed: String?, date: Date?) {
        date!!.let {
            etDateOfBirth.setText(Utils.convertCalendarToString(date))
        }
    }

    @OnClick(R.id.fragment_session_wise_choose_date_btn_ok)
    fun onOkButtonClicked(){

        if(datePicker.visibility == View.VISIBLE){
            mainActivity.showHUD()
            //Utils.showHideDateContainer(etDateOfBirth, datePicker, 500)
            Handler().postDelayed({
                gotoSessionWiseDataFragment()
                mainActivity.hideHUD()
            }, 600)
            return
        }

        gotoSessionWiseDataFragment()
    }

    fun gotoSessionWiseDataFragment(){
        var bundle = Bundle()
        bundle.putString(SessionWiseDataListFragment.SESSION_DATE, Utils.convertLocalDateToServerDate(etDateOfBirth.text.toString()))
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
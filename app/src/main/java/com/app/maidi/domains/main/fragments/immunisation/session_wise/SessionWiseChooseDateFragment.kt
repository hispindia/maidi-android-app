package com.app.maidi.domains.main.fragments.immunisation.session_wise

import android.animation.Animator
import android.os.Bundle
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
import com.app.maidi.utils.Utils
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.textfield.TextInputEditText

class SessionWiseChooseDateFragment : BaseFragment() {

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

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.session_wise_vaccination_due))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    @OnClick(R.id.fragment_session_wise_choose_date_v_date_of_birth)
    fun onDateOfBirthClicked(){
        Utils.showHideDateContainer(etDateOfBirth, datePicker, 500)
    }

    @OnClick(R.id.fragment_session_wise_choose_date_btn_ok)
    fun onOkButtonClicked(){
        mainActivity.transformFragment(R.id.activity_main_fl_content, SessionWiseDataListFragment())
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
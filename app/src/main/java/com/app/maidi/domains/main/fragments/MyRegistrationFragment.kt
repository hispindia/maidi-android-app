package com.app.maidi.domains.main.fragments

import android.app.DatePickerDialog
import android.os.Bundle
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
import com.app.maidi.domains.my_registration.list_my_registration.ListMyRegistrationActivity
import com.app.maidi.utils.DateUtils
import com.google.android.material.textfield.TextInputEditText
import org.joda.time.LocalDate
import java.util.*

class MyRegistrationFragment : BaseFragment(), DatePickerDialog.OnDateSetListener{

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    /*@BindView(R.id.fragment_my_registration_picker)
    lateinit var singleDateAndTimePicker : SingleDateAndTimePicker*/

    @BindView(R.id.fragment_my_registration_et_date_of_birth)
    lateinit var etDateOfBirth : TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_my_registration, container, false)
        ButterKnife.bind(this, viewGroup)

        etDateOfBirth.setText(DateUtils.convertCalendarToString(Calendar.getInstance().time))

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.my_immunisation_title))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    override fun onDateSet(view : DatePicker, year: Int, monthOfYear : Int, dayOfMonth: Int) {
        val date = LocalDate(year, monthOfYear + 1, dayOfMonth)
        etDateOfBirth.setText(DateUtils.convertCalendarToString(date.toDate()))
    }

    @OnClick(R.id.fragment_my_registration_v_date_of_birth)
    fun onDateOfBirthClicked(){
        var chooseDate = DateUtils.convertStringToLocalDate(etDateOfBirth.text.toString())
        var dateDialog = DatePickerDialog(context, this, chooseDate.getYear(), chooseDate.getMonthOfYear() - 1, chooseDate.getDayOfMonth())
        dateDialog.show()
        //controlPicker(500)
    }

    @OnClick(R.id.fragment_my_registration_btn_search)
    fun onSearchButtonClicked(){

        /*if(singleDateAndTimePicker.visibility == View.VISIBLE){
            mainActivity.showHUD()
            controlPicker(500)
            Handler().postDelayed({
                gotoListRegistrationScreen()
                mainActivity.hideHUD()
            }, 600)
            return
        }*/

        gotoListRegistrationScreen()

    }

    fun gotoListRegistrationScreen(){
        var bundle = Bundle()
        bundle.putString(ListMyRegistrationActivity.SEARCH_DATE, DateUtils.convertLocalDateToServerDate(etDateOfBirth.text.toString()))
        mainActivity.transformActivity(mainActivity, ListMyRegistrationActivity::class.java, false, bundle)
    }

    fun createPresenter() {
        mainPresenter = mainActivity.mainPresenter
    }

    /*fun controlPicker(duration: Int) {
        val expand = singleDateAndTimePicker.visibility != View.VISIBLE
        val prevHeight = singleDateAndTimePicker.height
        var height = 0
        if (expand) {
            val measureSpecParams = View.MeasureSpec.getSize(View.MeasureSpec.UNSPECIFIED)
            singleDateAndTimePicker.measure(measureSpecParams, measureSpecParams)
            height = singleDateAndTimePicker.measuredHeight
        }

        val valueAnimator = ValueAnimator.ofInt(prevHeight, height)
        valueAnimator.addUpdateListener { animation ->
            singleDateAndTimePicker.layoutParams.height = animation.animatedValue as Int
            singleDateAndTimePicker.requestLayout()
        }

        valueAnimator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                if (!expand) {
                    singleDateAndTimePicker.visibility = View.INVISIBLE
                    etDateOfBirth.setText(DateUtils.convertCalendarToString(singleDateAndTimePicker.date))
                }
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {
                if (expand) {
                    singleDateAndTimePicker.visibility = View.VISIBLE
                    singleDateAndTimePicker.selectDate(DateUtils.convertStringToCalendar(etDateOfBirth.text.toString()))
                }
            }
        })
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }*/
}
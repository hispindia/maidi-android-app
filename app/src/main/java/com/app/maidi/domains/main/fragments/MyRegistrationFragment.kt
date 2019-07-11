package com.app.maidi.domains.main.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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
import java.util.*

class MyRegistrationFragment : BaseFragment(){

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    @BindView(R.id.fragment_my_registration_picker)
    lateinit var singleDateAndTimePicker : SingleDateAndTimePicker

    @BindView(R.id.fragment_my_registration_et_date_of_birth)
    lateinit var etDateOfBirth : TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_my_registration, container, false)
        ButterKnife.bind(this, viewGroup)

        etDateOfBirth.setText(Utils.convertCalendarToString(Calendar.getInstance().time))

        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.my_immunisation_title))
    }

    @OnClick(R.id.fragment_my_registration_v_date_of_birth)
    fun onDateOfBirthClicked(){
        controlPicker(500)
    }

    @OnClick(R.id.fragment_my_registration_btn_search)
    fun onSearchButtonClicked(){
        mainActivity.transformActivity(mainActivity, ListMyRegistrationActivity::class.java, false)
    }

    fun createPresenter() {
        mainPresenter = mainActivity.mainPresenter
    }

    fun controlPicker(duration: Int) {
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
                    etDateOfBirth.setText(Utils.convertCalendarToString(singleDateAndTimePicker.date))
                }
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {
                if (expand) {
                    singleDateAndTimePicker.visibility = View.VISIBLE
                    singleDateAndTimePicker.selectDate(Utils.convertStringToCalendar(etDateOfBirth.text.toString()))
                }
            }
        })
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }
}
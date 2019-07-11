package com.app.maidi.domains.child_registration

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.utils.Utils
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.textfield.TextInputEditText
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.OrganUnit
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ChildRegistrationActivity : BaseActivity<ChildRegistrationView, ChildRegistrationPresenter>(){

    @Inject
    lateinit var childPresenter: ChildRegistrationPresenter

    lateinit var application: MainApplication

    @BindView(R.id.activity_child_registration_actionbar)
    lateinit var actionbar: RelativeLayout

    @BindView(R.id.activity_child_registration_picker)
    lateinit var singleDateAndTimePicker: SingleDateAndTimePicker

    @BindView(R.id.activity_child_registration_et_date_of_birth)
    lateinit var etDateOfBirth: TextInputEditText

    @BindView(R.id.activity_child_registration_actv_state)
    lateinit var actvState: AutoCompleteTextView

    @BindView(R.id.activity_child_registration_actv_district)
    lateinit var actvDistrict: AutoCompleteTextView

    @BindView(R.id.activity_child_registration_actv_block)
    lateinit var actvBlock: AutoCompleteTextView

    lateinit var ivBack: ImageView
    lateinit var stateUnits: List<OrganUnit>
    lateinit var districtUnits: MutableList<OrganUnit>
    lateinit var blockUnits: MutableList<OrganUnit>

    lateinit var stateAdapter: ChildRegistrationAdapter
    lateinit var districtAdapter: ChildRegistrationAdapter
    lateinit var blockAdapter: ChildRegistrationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_registration)
        ButterKnife.bind(this)

        ivBack = actionbar.findViewById(R.id.layout_actionbar_iv_action)
        ivBack.setOnClickListener({
            onBackPressed()
        })

        etDateOfBirth.setText(Utils.convertCalendarToString(Calendar.getInstance().time))
        updateStateDropdownData()
        updateDistrictDropdownData()
        updateBlockDropdownData()
    }

    fun updateStateDropdownData(){
        try {
            stateUnits = MetaDataController.getOrganisationUnitsStateLevel()
            stateAdapter = ChildRegistrationAdapter(this, android.R.layout.simple_list_item_1, stateUnits)
            actvState.threshold = 1
            actvState.setAdapter(stateAdapter)
            actvState.setOnItemClickListener({ adapterView, view, i, l ->
                actvState.setText((adapterView.getItemAtPosition(i) as OrganUnit).displayName)
                stateAdapter.selectedPosition = i
                updateDistrictDropdownData()
                updateBlockDropdownData()
            })
            if (stateUnits != null && stateUnits.size > 0) {
                actvState.setText(stateUnits.get(0).displayName)
                stateAdapter.selectedPosition = 0
            }
        }catch(ex: Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }
    }

    fun updateDistrictDropdownData(){

        districtUnits = arrayListOf<OrganUnit>()

        try{
            var stateUnit = stateAdapter.getItem(stateAdapter.selectedPosition)
            if(stateUnit.sChildren != null && !stateUnit.sChildren.isEmpty()){
                var districtIds = stateUnit.sChildren.trim().split(" ")

                if(districtIds != null && districtIds.size > 0){

                    for(districtId in districtIds){
                        var districtUnit = MetaDataController.getOrganisationUnitsDistrictLevel(districtId)
                        if(districtUnit != null){
                            districtUnits.add(districtUnit)
                        }
                    }

                    districtAdapter = ChildRegistrationAdapter(this, android.R.layout.simple_list_item_1, districtUnits)
                    actvDistrict.threshold = 1
                    actvDistrict.setAdapter(districtAdapter)
                    actvDistrict.setOnItemClickListener({ adapterView, view, i, l ->
                        actvDistrict.setText((adapterView.getItemAtPosition(i) as OrganUnit).displayName)
                        districtAdapter.selectedPosition = i
                        updateBlockDropdownData()
                    })
                    if(districtUnits != null && districtUnits.size > 0) {
                        actvDistrict.setText(districtUnits.get(0).displayName)
                        districtAdapter.selectedPosition = 0
                    }

                    return
                }

            }
        }catch(ex : Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }

        actvDistrict.setText("")
        districtAdapter = ChildRegistrationAdapter(this, android.R.layout.simple_list_item_1, districtUnits)
        actvDistrict.setAdapter(districtAdapter)
    }

    fun updateBlockDropdownData(){

        blockUnits = arrayListOf<OrganUnit>()

        try{
            var districtUnit = districtAdapter.getItem(districtAdapter.selectedPosition)
            if(districtUnit.sChildren != null && !districtUnit.sChildren.isEmpty()){
                var blockIds = districtUnit.sChildren.trim().split(" ")

                if(blockIds != null && blockIds.size > 0){

                    for(blockId in blockIds){
                        var blockUnit = MetaDataController.getOrganisationUnitsBlockLevel(blockId)
                        if(blockUnit != null){
                            blockUnits.add(blockUnit)
                        }
                    }

                    blockAdapter = ChildRegistrationAdapter(this, android.R.layout.simple_list_item_1, blockUnits)
                    actvBlock.threshold = 1
                    actvBlock.setAdapter(blockAdapter)
                    actvBlock.setOnItemClickListener({ adapterView, view, i, l ->
                        actvBlock.setText((adapterView.getItemAtPosition(i) as OrganUnit).displayName)
                        blockAdapter.selectedPosition = i
                    })
                    if(blockUnits != null && blockUnits.size > 0) {
                        actvBlock.setText(blockUnits.get(0).displayName)
                        blockAdapter.selectedPosition = 0
                    }

                    return
                }

            }
        }catch(ex : Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }

        actvBlock.setText("")
        blockAdapter = ChildRegistrationAdapter(this, android.R.layout.simple_list_item_1, blockUnits)
        actvBlock.setAdapter(blockAdapter)
    }

    @OnClick(R.id.activity_child_registration_v_state)
    fun onStateSpinnerClicked(){
        actvState.showDropDown()
    }

    @OnClick(R.id.activity_child_registration_v_district)
    fun onDistrictSpinnerClicked(){
        actvDistrict.showDropDown()
    }

    @OnClick(R.id.activity_child_registration_v_block)
    fun onBlockSpinnerClicked(){
        actvBlock.showDropDown()
    }

    @OnClick(R.id.activity_child_registration_v_date_of_birth)
    fun onDateOfBirthClicked(){
        controlPicker(500)
    }

    override fun createPresenter(): ChildRegistrationPresenter {
        application = getApplication() as MainApplication
        DaggerChildRegistrationComponent.builder()
            .appComponent(application.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return childPresenter
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
package com.app.maidi.domains.child_registration

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.app.maidi.utils.Constants
import com.app.maidi.utils.Utils
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.textfield.TextInputEditText
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.*
import org.joda.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChildRegistrationActivity : BaseActivity<ChildRegistrationView, ChildRegistrationPresenter>(){

    @Inject
    lateinit var childPresenter: ChildRegistrationPresenter

    lateinit var application: MainApplication

    @BindView(R.id.activity_child_registration_ll_content)
    lateinit var llContent: LinearLayout

    @BindView(R.id.activity_child_registration_actionbar)
    lateinit var actionbar: RelativeLayout

    @BindView(R.id.activity_child_registration_et_first_name)
    lateinit var etFirstName: TextInputEditText

    @BindView(R.id.activity_child_registration_et_last_name)
    lateinit var etLastName: TextInputEditText

    @BindView(R.id.activity_child_registration_et_gender)
    lateinit var etGender: EditText

    @BindView(R.id.activity_child_registration_et_mother_name)
    lateinit var etMotherName: TextInputEditText

    @BindView(R.id.activity_child_registration_picker)
    lateinit var singleDateAndTimePicker: SingleDateAndTimePicker

    @BindView(R.id.activity_child_registration_et_date_of_birth)
    lateinit var etDateOfBirth: TextInputEditText

    @BindView(R.id.activity_child_registration_et_state)
    lateinit var etState: EditText

    @BindView(R.id.activity_child_registration_et_district)
    lateinit var etDistrict: EditText

    @BindView(R.id.activity_child_registration_et_block)
    lateinit var etBlock: EditText

    @BindView(R.id.activity_child_registration_et_village)
    lateinit var etVillage: EditText

    @BindView(R.id.activity_child_registration_sp_gender)
    lateinit var spGender: Spinner

    @BindView(R.id.activity_child_registration_sp_state)
    lateinit var spState: Spinner

    @BindView(R.id.activity_child_registration_sp_district)
    lateinit var spDistrict: Spinner

    @BindView(R.id.activity_child_registration_sp_block)
    lateinit var spBlock: Spinner

    @BindView(R.id.activity_child_registration_sp_village)
    lateinit var spVillage: Spinner

    val gender = listOf<String>("Female", "Male")
    var isStateClicked = false
    var isDistrictClicked = false
    var isBlockClicked = false
    var trackedEntityAttributeValueMap : Map<String, TrackedEntityAttributeValue> = HashMap<String, TrackedEntityAttributeValue>()

    lateinit var trackedEntityInstance: TrackedEntityInstance
    lateinit var enrollment: Enrollment

    lateinit var ivBack: ImageView
    lateinit var currentProgram: Program
    lateinit var topUnit: OrganisationUnit
    lateinit var stateUnits: List<OrganUnit>
    lateinit var districtUnits: MutableList<OrganUnit>
    lateinit var blockUnits: MutableList<OrganUnit>
    lateinit var villageUnits: MutableList<OrganUnit>

    lateinit var genderAdapter: ArrayAdapter<String>
    lateinit var stateAdapter: ChildRegistrationAdapter
    lateinit var districtAdapter: ChildRegistrationAdapter
    lateinit var blockAdapter: ChildRegistrationAdapter
    lateinit var villageAdapter: ChildRegistrationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_registration)
        ButterKnife.bind(this)

        setupEditTextKeyboard(llContent, this)

        topUnit = MetaDataController.getTopLevelOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.BENEFICIARY_CHILD_REGISTRATION)

        ivBack = actionbar.findViewById(R.id.layout_actionbar_iv_action)
        ivBack.setOnClickListener({
            onBackPressed()
        })

        etDateOfBirth.setText(Utils.convertCalendarToString(Calendar.getInstance().time))
        updateGenderDropdownData()
        updateStateDropdownData()
        updateDistrictDropdownData()
        updateBlockDropdownData()
        updateVillageDropdownData()
    }

    fun updateGenderDropdownData(){
        genderAdapter = ArrayAdapter(this, R.layout.item_dropdown, R.id.item, gender)
        spGender.setAdapter(genderAdapter)
        spGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                etGender.setText(adapterView!!.getItemAtPosition(pos) as String)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        etGender.setText(gender.get(0))
    }

    fun updateStateDropdownData(){
        try {
            val units = arrayListOf<OrganUnit>()
            stateUnits = MetaDataController.getOrganisationUnitsStateLevel()
            if(stateUnits != null && stateUnits.size > 0){
                var blank = OrganUnit()
                blank.level = -1
                blank.displayName = resources.getString(R.string.blank_choose)
                units.add(blank)
                units.addAll(stateUnits)
            }
            stateAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, units)
            spState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    var unit = adapterView!!.getItemAtPosition(pos) as OrganUnit
                    if(unit.level != -1)
                        etState.setText(unit.displayName)
                    else
                        etState.setText("")
                    stateAdapter.selectedPosition = pos
                    updateDistrictDropdownData()
                    updateBlockDropdownData()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
            spState.setAdapter(stateAdapter)
            /*if (stateUnits != null && stateUnits.size > 0) {
                etState.setText(stateUnits.get(0).displayName)
                stateAdapter.selectedPosition = 0
            }*/
        }catch(ex: Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }
    }

    fun updateDistrictDropdownData(){

        var units = arrayListOf<OrganUnit>()
        districtUnits = arrayListOf<OrganUnit>()

        try{
            if(stateAdapter.getItem(stateAdapter.selectedPosition) != null){
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

                        if(districtUnits != null && districtUnits.size > 0){
                            var blank = OrganUnit()
                            blank.level = -1
                            blank.displayName = resources.getString(R.string.blank_choose)
                            units.add(blank)
                            units.addAll(districtUnits)
                        }

                        districtAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, units)

                        spDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                var unit = adapterView!!.getItemAtPosition(pos) as OrganUnit
                                if(unit.level != -1)
                                    etDistrict.setText(unit.displayName)
                                else
                                    etDistrict.setText("")
                                districtAdapter.selectedPosition = pos
                                updateBlockDropdownData()
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }
                        }

                        spDistrict.setAdapter(districtAdapter)
                        /*if(districtUnits != null && districtUnits.size > 0) {
                            actvDistrict.setText(districtUnits.get(0).displayName)
                            districtAdapter.selectedPosition = 0
                        }*/

                        return
                    }

                }
            }
        }catch(ex : Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }

        etDistrict.setText("")
        districtAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, districtUnits)
        spDistrict.setAdapter(districtAdapter)
    }

    fun updateBlockDropdownData(){
        var units = arrayListOf<OrganUnit>()
        blockUnits = arrayListOf<OrganUnit>()

        try{
            if(districtAdapter.getItem(districtAdapter.selectedPosition) != null){
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

                        if(blockUnits != null && blockUnits.size > 0){
                            var blank = OrganUnit()
                            blank.level = -1
                            blank.displayName = resources.getString(R.string.blank_choose)
                            units.add(blank)
                            units.addAll(blockUnits)
                        }

                        blockAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, units)

                        spBlock.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                var unit = adapterView!!.getItemAtPosition(pos) as OrganUnit
                                if(unit.level != -1)
                                    etBlock.setText(unit.displayName)
                                else
                                    etBlock.setText("")
                                blockAdapter.selectedPosition = pos
                                updateVillageDropdownData()
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }
                        }

                        spBlock.setAdapter(blockAdapter)
                        /*if(blockUnits != null && blockUnits.size > 0) {
                            actvBlock.setText(blockUnits.get(0).displayName)
                            blockAdapter.selectedPosition = 0
                        }*/

                        return
                    }

                }
            }
        }catch(ex : Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }

        etBlock.setText("")
        blockAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, blockUnits)
        spBlock.setAdapter(blockAdapter)
    }

    fun updateVillageDropdownData(){
        var units = arrayListOf<OrganUnit>()
        villageUnits = arrayListOf<OrganUnit>()

        try{
            if(blockAdapter.getItem(blockAdapter.selectedPosition) != null){
                var blockUnit = blockAdapter.getItem(blockAdapter.selectedPosition)
                if(blockUnit.sChildren != null && !blockUnit.sChildren.isEmpty()){
                    var villageIds = blockUnit.sChildren.trim().split(" ")

                    if(villageIds != null && villageIds.size > 0){

                        for(villageId in villageIds){
                            var villageUnit = MetaDataController.getOrganisationUnitsVillageLevel(villageId)
                            if(villageUnit != null){
                                villageUnits.add(villageUnit)
                            }
                        }

                        if(villageUnits != null && villageUnits.size > 0){
                            var blank = OrganUnit()
                            blank.level = -1
                            blank.displayName = resources.getString(R.string.blank_choose)
                            units.add(blank)
                            units.addAll(villageUnits)
                        }

                        villageAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, units)

                        spVillage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                var unit = adapterView!!.getItemAtPosition(pos) as OrganUnit
                                if(unit.level != -1)
                                    etVillage.setText(unit.displayName)
                                else
                                    etVillage.setText("")
                                villageAdapter.selectedPosition = pos
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {

                            }
                        }

                        spVillage.setAdapter(villageAdapter)
                        /*if(blockUnits != null && blockUnits.size > 0) {
                            actvBlock.setText(blockUnits.get(0).displayName)
                            blockAdapter.selectedPosition = 0
                        }*/

                        return
                    }

                }
            }
        }catch(ex : Exception){
            Log.e(this.localClassName + " Exception", ex.toString())
        }

        etVillage.setText("")
        villageAdapter = ChildRegistrationAdapter(this, R.layout.item_dropdown, villageUnits)
        spVillage.setAdapter(villageAdapter)
    }

    @OnClick(R.id.activity_child_registration_v_gender)
    fun onGenderSpinnerClicked(){
        spGender.performClick()
    }

    @OnClick(R.id.activity_child_registration_v_date_of_birth)
    fun onDateOfBirthClicked(){
        controlPicker(500)
    }

    @OnClick(R.id.activity_child_registration_btn_submit)
    fun onSubmitButtonClicked(){
        if(validate()){
            confirmSave()
        }
    }

    fun validate() : Boolean{
        try {
            /*if (isMapEmpty(trackedEntityAttributeValueMap)) {
                Toast.makeText(this, resources.getString(R.string.form_is_empty), Toast.LENGTH_LONG).show()
                return false
            }*/

            if (!TrackerController.validateUniqueValues(trackedEntityAttributeValueMap, topUnit.id)) {
                var listUniqueInvalidFields =
                    TrackerController.getNotValidatedUniqueValues(trackedEntityAttributeValueMap, topUnit.id)
                var listInvalidAttributes = " "
                for (value in listUniqueInvalidFields) {
                    listInvalidAttributes += value + " "
                }
                Toast.makeText(
                    this,
                    String.format("Invalid unique value: %s", listInvalidAttributes),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }

            if (etFirstName.text!!.isEmpty()) {
                Toast.makeText(this, "Please fill the first name of child", Toast.LENGTH_SHORT).show()
                return false
            }

            if (etLastName.text!!.isEmpty()) {
                Toast.makeText(this, "Please fill the last name of child", Toast.LENGTH_SHORT).show()
                return false
            }

            if (etMotherName.text!!.isEmpty()) {
                Toast.makeText(this, "Please fill the mother's name", Toast.LENGTH_SHORT).show()
                return false
            }
        }catch(ex : Exception){
            Log.d("Validate Exception", ex.toString())
        }

        return true
    }

    fun confirmSave() {
        try {
            showHUD()
            if(singleDateAndTimePicker.visibility == View.VISIBLE){
                controlPicker(500)
            }
            Handler().postDelayed({

                var enrollmentDate = LocalDate.now()
                var incidentDate = LocalDate.now()

                trackedEntityInstance = TrackedEntityInstance(currentProgram, topUnit.id)
                enrollment = Enrollment(
                    topUnit.id,
                    trackedEntityInstance.trackedEntityInstance,
                    currentProgram,
                    enrollmentDate.toString(Constants.SERVER_DATE_PATTERN),
                    incidentDate.toString(Constants.SERVER_DATE_PATTERN)
                )

                var trackedEntityAttributeValues =
                    childPresenter.getTrackedEntityAttributeValues(currentProgram, trackedEntityInstance)
                enrollment.attributes = trackedEntityAttributeValues
                setupTrackedEntityAttributeValues()
                childPresenter.saveTrackedEntityOffline(trackedEntityInstance, enrollment, currentProgram)
                hideHUD()
                Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
                onBackPressed()
            }, 500)
        }catch(ex : Exception){
            Log.d("Save Exception", ex.toString())
            hideHUD()
        }
    }

    fun setupTrackedEntityAttributeValues(){
        var name = etFirstName.text.toString() + " " + etLastName.text.toString()
        var gender = if(etGender.text.toString().equals("Female")) "F" else "M"
        var dateOfBirth = Utils.convertLocalDateToServerDate(etDateOfBirth.text.toString())
        var motherName = etMotherName.text.toString()
        var mobileNumber = MetaDataController.getUserAccount().phoneNumber
        var subUnit: OrganUnit? = null

        if(!etState.text.isEmpty()){
            subUnit = stateAdapter.getItem(stateAdapter.selectedPosition)
        }

        if(!etDistrict.text.isEmpty()){
            subUnit = districtAdapter.getItem(districtAdapter.selectedPosition)
        }

        if(!etBlock.text.isEmpty()){
            subUnit = blockAdapter.getItem(blockAdapter.selectedPosition)
        }

        if(!etVillage.text.isEmpty()){
            subUnit = villageAdapter.getItem(villageAdapter.selectedPosition)
        }

        var trackedEntityAttributeValues = enrollment.attributes
        var programEntityAttributes = MetaDataController.getProgramTrackedEntityAttributes(currentProgram.uid)
        for(programEntityAttribute in programEntityAttributes){
            var trackedEntityAttribute = MetaDataController.getTrackedEntityAttribute(programEntityAttribute.trackedEntityAttributeId)
            var trackedEntityAttributeValue = TrackedEntityAttributeValue()

            if(trackedEntityAttribute.displayName.contains("Name")){
                createNewTrackedValue(trackedEntityAttributeValues, trackedEntityAttributeValue, programEntityAttribute, name)
            }

            if(trackedEntityAttribute.displayName.contains("Gender")){
                createNewTrackedValue(trackedEntityAttributeValues, trackedEntityAttributeValue, programEntityAttribute, gender)
            }

            if(trackedEntityAttribute.displayName.contains("Date of Birth")){
                createNewTrackedValue(trackedEntityAttributeValues, trackedEntityAttributeValue, programEntityAttribute, dateOfBirth)
            }

            if(trackedEntityAttribute.displayName.contains("Mother")){
                createNewTrackedValue(trackedEntityAttributeValues, trackedEntityAttributeValue, programEntityAttribute, motherName)
            }

            if(mobileNumber != null && !mobileNumber.isEmpty()){
                if(trackedEntityAttribute.displayName.contains("Mobile")){
                    createNewTrackedValue(trackedEntityAttributeValues, trackedEntityAttributeValue, programEntityAttribute, mobileNumber)
                }
            }

            if(subUnit != null) {
                if (trackedEntityAttribute.displayName.contains("Residential")) {
                    createNewTrackedValue(
                        trackedEntityAttributeValues,
                        trackedEntityAttributeValue,
                        programEntityAttribute,
                        subUnit.uid
                    )
                }
            }
        }

        enrollment.attributes = trackedEntityAttributeValues
    }

    private fun createNewTrackedValue(trackedEntityAttributeValues: MutableList<TrackedEntityAttributeValue>, trackedEntityAttributeValue : TrackedEntityAttributeValue, pea : ProgramTrackedEntityAttribute, value: String){
        trackedEntityAttributeValue.trackedEntityAttributeId = pea.trackedEntityAttributeId
        trackedEntityAttributeValue.trackedEntityInstanceId = trackedEntityInstance.getUid()
        trackedEntityAttributeValue.value = value
        trackedEntityAttributeValues.add(trackedEntityAttributeValue)
    }

    private fun isMapEmpty(
        trackedEntityAttributeValueMap: Map<String, TrackedEntityAttributeValue>
    ): Boolean {
        var isEmpty = true
        for (key in trackedEntityAttributeValueMap.keys) {
            val value = trackedEntityAttributeValueMap[key]
            if (value!!.getValue() != null && value!!.getValue() != "") {
                isEmpty = false
            }
        }
        return isEmpty
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
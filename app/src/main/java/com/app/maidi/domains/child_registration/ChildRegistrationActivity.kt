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

class ChildRegistrationActivity : BaseActivity<ChildRegistrationView, ChildRegistrationPresenter>() {

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

    @BindView(R.id.activity_child_registration_et_social_category)
    lateinit var etSocialCategory: EditText

    @BindView(R.id.activity_child_registration_et_mother_name)
    lateinit var etMotherName: TextInputEditText

    @BindView(R.id.activity_child_registration_picker)
    lateinit var singleDateAndTimePicker: SingleDateAndTimePicker

    @BindView(R.id.activity_child_registration_et_date_of_birth)
    lateinit var etDateOfBirth: TextInputEditText

    @BindView(R.id.activity_child_registration_et_mobile_number)
    lateinit var etMobileNumber: TextInputEditText

    @BindView(R.id.activity_child_registration_et_father_name)
    lateinit var etFatherName: TextInputEditText

    @BindView(R.id.activity_child_registration_et_alternate_contact_number)
    lateinit var etAlternateContact: TextInputEditText

    @BindView(R.id.activity_child_registration_et_address)
    lateinit var etAddress: TextInputEditText

    @BindView(R.id.activity_child_registration_sp_gender)
    lateinit var spGender: Spinner

    @BindView(R.id.activity_child_registration_sp_social_category)
    lateinit var spCategory: Spinner

    var trackedEntityAttributeValueMap: Map<String, TrackedEntityAttributeValue> =
        HashMap<String, TrackedEntityAttributeValue>()

    lateinit var trackedEntityInstance: TrackedEntityInstance
    lateinit var enrollment: Enrollment

    lateinit var ivBack: ImageView
    lateinit var currentProgram: Program
    lateinit var topUnit: OrganisationUnit
    lateinit var genderAttribute: TrackedEntityAttribute
    lateinit var socialAttribute: TrackedEntityAttribute
    lateinit var genderOptions: List<Option>
    lateinit var socialOptions: List<Option>
    lateinit var socialList: ArrayList<Option>

    lateinit var genderAdapter: OptionAdapter
    lateinit var socialAdapter: OptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_registration)

        topUnit = MetaDataController.getTopAssignedOrganisationUnit()
        currentProgram = MetaDataController.getProgramByName(Constants.IMMUNISATION)
        socialList = arrayListOf()

        ButterKnife.bind(this)

        setupEditTextKeyboard(llContent, this)

        ivBack = actionbar.findViewById(R.id.layout_actionbar_iv_action)
        ivBack.setOnClickListener({
            onBackPressed()
        })

        etDateOfBirth.setText(Utils.convertCalendarToString(Calendar.getInstance().time))

        genderAttribute = MetaDataController.getGenderAttribute()
        socialAttribute = MetaDataController.getSocialAttribute()
        genderOptions = MetaDataController.getOptionsFollowOptionSet(genderAttribute.optionSet)
        socialOptions = MetaDataController.getOptionsFollowOptionSet(genderAttribute.optionSet)
        if(socialOptions != null && socialOptions.size > 0){
            var blank = Option()
            blank.sortIndex = -1
            blank.displayName = resources.getString(R.string.blank_choose)
            socialList.add(blank)
            socialList.addAll(socialOptions)
        }

        updateGenderDropdownData()
        updateSocialDropdownData()
    }

    fun updateGenderDropdownData() {
        genderAdapter = OptionAdapter(this, R.layout.item_dropdown, genderOptions)
        spGender.setAdapter(genderAdapter)
        spGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                var option = adapterView!!.getItemAtPosition(pos) as Option
                etGender.setText(option.displayName)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        etGender.setText(genderOptions.get(0).displayName)
    }

    fun updateSocialDropdownData() {
        socialAdapter = OptionAdapter(this, R.layout.item_dropdown, socialList)
        spCategory.setAdapter(socialAdapter)
        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                var option = adapterView!!.getItemAtPosition(pos) as Option
                if(option.sortIndex != -1)
                    etSocialCategory.setText(option.displayName)
                else
                    etSocialCategory.setText("")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

    }

    @OnClick(R.id.activity_child_registration_v_gender)
    fun onGenderSpinnerClicked() {
        spGender.performClick()
    }

    @OnClick(R.id.activity_child_registration_v_date_of_birth)
    fun onDateOfBirthClicked() {
        controlPicker(500)
    }

    @OnClick(R.id.activity_child_registration_btn_submit)
    fun onSubmitButtonClicked() {
        if (validate()) {
            confirmSave()
        }
    }

    fun validate(): Boolean {
        try {

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

            if (etMobileNumber.text!!.isEmpty()) {
                Toast.makeText(this, "Please fill the mobile number", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (ex: Exception) {
            Log.d("Validate Exception", ex.toString())
        }

        return true
    }

    fun confirmSave() {
        try {
            showHUD()
            if (singleDateAndTimePicker.visibility == View.VISIBLE) {
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
        } catch (ex: Exception) {
            Log.d("Save Exception", ex.toString())
            hideHUD()
        }
    }

    fun setupTrackedEntityAttributeValues() {
        var name = etFirstName.text.toString() + " " + etLastName.text.toString()
        var dateOfBirth = Utils.convertLocalDateToServerDate(etDateOfBirth.text.toString())
        var motherName = etMotherName.text.toString()
        var mobileNumber = etMobileNumber.text.toString()
        var fatherName = etFatherName.text.toString()
        var alternateContact = etAlternateContact.text.toString()
        var address = etAddress.text.toString()
        var socialCategory: Option? = null
        var gender : Option? = null

        if (!etGender.text.isEmpty()) {
            gender = spGender.selectedItem as Option
        }

        if (!etSocialCategory.text.isEmpty()) {
            socialCategory = spCategory.selectedItem as Option
        }

        var trackedEntityAttributeValues = enrollment.attributes
        var programEntityAttributes = MetaDataController.getProgramTrackedEntityAttributes(currentProgram.uid)
        for (programEntityAttribute in programEntityAttributes) {
            var trackedEntityAttribute =
                MetaDataController.getTrackedEntityAttribute(programEntityAttribute.trackedEntityAttributeId)
            var trackedEntityAttributeValue = TrackedEntityAttributeValue()

            if (trackedEntityAttribute.displayName.contains("Name")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    name
                )
            }

            if (trackedEntityAttribute.displayName.contains("Gender")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    gender!!.code
                )
            }

            if(socialCategory != null) {
                if (trackedEntityAttribute.displayName.contains("Social")) {
                    createNewTrackedValue(
                        trackedEntityAttributeValues,
                        trackedEntityAttributeValue,
                        programEntityAttribute,
                        socialCategory!!.code
                    )
                }
            }

            if (trackedEntityAttribute.displayName.contains("Date of Birth")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    dateOfBirth
                )
            }

            if (trackedEntityAttribute.displayName.contains("Mother")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    motherName
                )
            }

            if (trackedEntityAttribute.displayName.contains("Mobile")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    mobileNumber
                )
            }

            if (trackedEntityAttribute.displayName.contains("Father")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    fatherName
                )
            }

            if (trackedEntityAttribute.displayName.contains("Alternate")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    alternateContact
                )
            }

            if (trackedEntityAttribute.displayName.contains("Address")) {
                createNewTrackedValue(
                    trackedEntityAttributeValues,
                    trackedEntityAttributeValue,
                    programEntityAttribute,
                    address
                )
            }

        }

        enrollment.attributes = trackedEntityAttributeValues
    }

    private fun createNewTrackedValue(
        trackedEntityAttributeValues: MutableList<TrackedEntityAttributeValue>,
        trackedEntityAttributeValue: TrackedEntityAttributeValue,
        pea: ProgramTrackedEntityAttribute,
        value: String
    ) {
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

        valueAnimator.addListener(object : Animator.AnimatorListener {
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
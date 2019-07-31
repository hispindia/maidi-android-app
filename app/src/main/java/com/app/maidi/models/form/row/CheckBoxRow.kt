package com.app.maidi.models.form.row

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.app.maidi.R
import com.app.maidi.models.form.DataEntryRow
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.utils.api.ValueType

class CheckBoxRow : DataEntryRow{

    lateinit var tvTitle: TextView
    lateinit var cbCheckbox: CheckBox

    constructor(context: Context?, valueType: ValueType, dataElement: DataElement) : super(context) {init(valueType, dataElement)}
    constructor(context: Context?, attrs: AttributeSet?, valueType: ValueType, dataElement: DataElement) : super(context, attrs) { init(valueType, dataElement) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, valueType: ValueType, dataElement: DataElement) : super(context, attrs, defStyleAttr) { init(valueType, dataElement) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, valueType: ValueType, dataElement: DataElement) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ){ init(valueType, dataElement) }

    override fun init(valueType: ValueType, dataElement: DataElement) {
        super.init(valueType, dataElement)
        LayoutInflater.from(context).inflate(R.layout.row_checkbox_layout, this, true)
        tvTitle = findViewById(R.id.row_checkbox_tv_title)
        cbCheckbox = findViewById(R.id.row_checkbox_cb_checkbox)
    }

    fun isCheckboxSelected() : Boolean{
        return cbCheckbox.isSelected
    }

}
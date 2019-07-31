package com.app.maidi.models.form.row

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.app.maidi.R
import com.app.maidi.models.form.DataEntryRow
import com.google.android.material.textfield.TextInputEditText
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.utils.api.ValueType

class InputTextRow : DataEntryRow {

    lateinit var tietInput: TextInputEditText

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
        tietInput = findViewById(R.id.row_input_email_tiet_input)
    }

    fun setTitle(title: String){
        tietInput.hint = title
    }

    fun getValue() : String {
        return tietInput.text.toString()
    }

}
package com.app.maidi.models.form

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.utils.api.ValueType

open class DataEntryRow : LinearLayout{

    lateinit var valueType : ValueType
    lateinit var dataElement: DataElement

    var validationError: Int = -1

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    open fun init(valueType: ValueType, dataElement: DataElement){
        this.valueType = valueType
        this.dataElement = dataElement
    }

}
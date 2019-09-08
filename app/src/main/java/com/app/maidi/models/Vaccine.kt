package com.app.maidi.models

import android.os.Parcel
import android.os.Parcelable
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.DataValue

open class Vaccine {

    var dataElement: DataElement
    var dataValue: DataValue?
    var dueDate: String
    var isInjected: Boolean
    var isShowed: Boolean = false

    constructor(dataElement: DataElement, dataValue: DataValue?, dueDate : String, isInjected : Boolean){
        this.dataElement = dataElement
        this.dataValue = dataValue
        this.dueDate = dueDate
        this.isInjected = isInjected
    }
}

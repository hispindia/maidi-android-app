package com.app.maidi.models

import android.os.Parcel
import android.os.Parcelable
import org.hisp.dhis.android.sdk.persistence.models.DataElement

class Vaccine {

    var dataElement: DataElement
    var dueDate: String
    var isInjected: Boolean

    constructor(dataElement: DataElement, dueDate : String, isInjected : Boolean){
        this.dataElement = dataElement
        this.dueDate = dueDate
        this.isInjected = isInjected
    }
}

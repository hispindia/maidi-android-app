package com.app.maidi.models

import org.hisp.dhis.android.sdk.persistence.models.DataElement

data class Vaccine(
    var dataElement: DataElement,
    var dueDate: String,
    var isInjected: Boolean)
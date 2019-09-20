package com.app.maidi.models.database

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.structure.BaseModel

class Organisation: BaseModel() {

    @Column(name = "id")
    var id: String? = null
}
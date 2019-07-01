package com.app.maidi.models.database

import com.app.maidi.database.MaidiDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

//@Table(database = MaidiDatabase::class)
class Organisation: BaseModel() {

    @Column(name = "id")
    var id: String? = null
}
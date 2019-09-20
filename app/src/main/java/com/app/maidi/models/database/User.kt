package com.app.maidi.models.database

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.structure.BaseModel

class User : BaseModel(){

    @PrimaryKey
    @Column(name = "id")
    var id: String? = null

    @Column(name = "displayName")
    var displayName: String? = null

    @Column(name = "firstName")
    var firstName: String? = null

    @Column(name = "subname")
    var surname: String? = null

    @Column(name = "created")
    var created: String? = null

    @Column(name = "lastUpdated")
    var lastUpdated: String? = null

    @Column(name = "teiSearchOrganisationUnits")
    var terSearchOrganisationUnits: List<String>? = null

    @Column(name = "organisationUnits")
    var organisationUnits: List<Organisation>? = null

    @Column(name = "programs")
    var programs: List<String>? = null
}
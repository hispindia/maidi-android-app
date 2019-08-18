package com.app.maidi.models

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import org.hisp.dhis.android.sdk.persistence.models.Enrollment
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class ImmunisationCard() {

    lateinit var trackedEntityInstance : TrackedEntityInstance
    lateinit var enrollment: Enrollment
    lateinit var vaccineList: List<Vaccine>
    var isShowContent = false
}
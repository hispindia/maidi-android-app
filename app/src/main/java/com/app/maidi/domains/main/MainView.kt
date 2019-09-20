package com.app.maidi.domains.main

import com.app.maidi.domains.base.BaseView
import com.app.maidi.models.Dose
import com.app.maidi.models.ImmunisationCard
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

interface MainView : BaseView {
    fun getRegisteredBeneficariesInstances(trackedEntityInstances : List<TrackedEntityInstance>)
    fun getAefiTrackedEntityInstances(trackedEntityInstances : List<TrackedEntityInstance>)
    fun getSurveyEvents(events: List<Event>)
    fun getWorkplanEvents(events: List<Event>)
    fun getImmunisationCardListSuccess(immunisationList: List<ImmunisationCard>)
    fun getProgramDataElements(dataElements: List<DataElement>)
    fun getTotalDoseList(doseList: List<Dose>)
    fun getSessionWiseDataListSuccess(sessionWiseList: List<ImmunisationCard>)
    fun getApiFailed(exception: APIException)
}
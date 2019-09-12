package com.app.maidi.domains.main

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.models.Dose
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.models.Vaccine
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import com.app.maidi.utils.Constants
import com.app.maidi.utils.DateUtils
import com.app.maidi.utils.MethodUtils
import io.reactivex.disposables.Disposable
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.job.JobExecutor
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.ResponseHolder
import org.hisp.dhis.android.sdk.persistence.models.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class MainPresenter : BasePresenter<MainView> {

    var accountService: AccountService
    var networkProvider: NetworkProvider
    var disposable : Disposable? = null

    @Inject
    constructor(networkProvider: NetworkProvider, accountService: AccountService){
        this.networkProvider = networkProvider
        this.accountService = accountService
    }

    // AEFI MODULE
    fun getAefiTrackedEntityInstances(orgUnitId: String, aefiProgramId: String, immunisationProgramId: String){
        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {
                    var trackedEntityInstances = arrayListOf<TrackedEntityInstance>()
                    var aefiInstances = listOf<TrackedEntityInstance>()
                    var immunisationInstances = listOf<TrackedEntityInstance>()

                    aefiInstances = TrackerController.queryLocalTrackedEntityInstances(orgUnitId, aefiProgramId)
                    immunisationInstances = TrackerController.queryLocalTrackedEntityInstances(orgUnitId, immunisationProgramId)
                    trackedEntityInstances.addAll(aefiInstances)
                    trackedEntityInstances.addAll(immunisationInstances)

                    if(isViewAttached)
                        view.getAefiTrackedEntityInstances(trackedEntityInstances)

                    return Any()
                }
            })
        }catch(exception : APIException){
            if(isViewAttached)
                view.getApiFailed(exception)
        }
    }



    // IMMUNISATION SUB-MODULE
    fun getImmunisationTrackedEntityInstances(orgUnitId: String, programId: String){

        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {

                    var immunisationCardList = arrayListOf<ImmunisationCard>()
                    var trackedEntityInstances = listOf<TrackedEntityInstance>()

                    trackedEntityInstances = TrackerController.queryLocalTrackedEntityInstances(orgUnitId, programId)

                    var programStage = TrackerController.getProgramStageByName(programId, Constants.IMMUNISATION)
                    var programDataElements = TrackerController.getProgramStageDataElements(programStage.uid)
                    var assignedDataElements = arrayListOf<DataElement>()

                    for(dataElememt in programDataElements){
                        var element = TrackerController.getDataElement(dataElememt.dataelement)
                        if(element != null){
                            assignedDataElements.add(element)
                        }
                    }

                    for(trackedEntityInstance in trackedEntityInstances){

                        var immunisationCard = ImmunisationCard()
                        var vaccineList = arrayListOf<Vaccine>()
                        var injectedVaccineList = arrayListOf<Vaccine>()

                        for(dataElement in assignedDataElements){
                            vaccineList.add(Vaccine(dataElement, null,"", false))
                        }

                        var enrollment = TrackerController.getEnrollment(programId, trackedEntityInstance)

                        if(enrollment != null){
                            var events = enrollment.getEventThoughOrganisationUnit(orgUnitId)
                            if(events != null){
                                for(event in events){
                                    if(event.programStageId.equals(programStage.uid)){
                                        var dataValues = event.dataValues
                                        for(vaccine in vaccineList){
                                            for(dataValue in dataValues){
                                                if(dataValue.dataElement.equals(vaccine.dataElement.uid)){
                                                    if(!vaccine.dataElement.displayName.contains("Show")) {
                                                        if(!dataValue.value.isEmpty()){
                                                            var item = Vaccine(
                                                                vaccine.dataElement,
                                                                dataValue,
                                                                event.dueDate,
                                                                if(dataValue.value.equals("true")) true else false
                                                            )
                                                            if(item.isInjected)
                                                                injectedVaccineList.add(item)
                                                        }
                                                    }
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            immunisationCard.enrollment = enrollment
                        }

                        var scheduleVaccineList = MethodUtils.createScheduleVaccineList(
                            LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(immunisationCard.enrollment.incidentDate)),
                            LocalDate.now(),
                            injectedVaccineList,
                            vaccineList
                        )

                        immunisationCard.trackedEntityInstance = trackedEntityInstance
                        immunisationCard.vaccineList = scheduleVaccineList//.subList(0, 6)

                        immunisationCardList.add(immunisationCard)

                    }

                    if(isViewAttached)
                        view.getImmunisationCardListSuccess(immunisationCardList)

                    return Any()
                }
            })
        }catch(exception : APIException){
            if(isViewAttached)
                view.getApiFailed(exception)
        }

    }



    // ------- SESSION WISE SUB-MODULE ----------- //
    fun getSessionWiseDatas(orgUnitId: String, programId: String, sessionDate: String){
        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {

                    var allDataElements = getProgramDataElement(programId)
                    var trackedEntityInstances = TrackerController.queryLocalTrackedEntityInstances(orgUnitId, programId)
                    var filtedTrackedEntityInstances = arrayListOf<TrackedEntityInstance>()

                    for(instance in trackedEntityInstances){
                        var enrollment = TrackerController.getEnrollment(programId, instance)
                        if(enrollment != null){
                            var incidentLocalDate = LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(enrollment.incidentDate))
                            var sessionLocalDate = LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(sessionDate))
                            if(sessionLocalDate.isAfter(incidentLocalDate) ||
                                sessionLocalDate.isEqual(incidentLocalDate)){
                                    filtedTrackedEntityInstances.add(instance)
                            }
                        }
                    }

                    var sessionDataElements = getListDataElementForSession(orgUnitId, programId, sessionDate, filtedTrackedEntityInstances, allDataElements)
                    var immunisationCardList = getSessionWiseDataList(orgUnitId, programId, sessionDate, filtedTrackedEntityInstances, sessionDataElements, allDataElements)
                    var doseList = getTotalDoseList(orgUnitId, programId, sessionDataElements, immunisationCardList)

                    if(isViewAttached)
                        view.getProgramDataElements(sessionDataElements)

                    if(isViewAttached)
                        view.getSessionWiseDataListSuccess(immunisationCardList)

                    if(isViewAttached) {
                        view.getTotalDoseList(doseList)
                    }

                    return Any()
                }
            })
        }catch(exception : APIException){
            if(isViewAttached)
                view.getApiFailed(exception)
        }
    }

    fun getProgramDataElement(programId: String) : List<DataElement>{
        var programStage = TrackerController.getProgramStageByName(programId, Constants.IMMUNISATION)
        var programDataElements = TrackerController.getProgramStageDataElements(programStage.uid)
        var assignedDataElements = arrayListOf<DataElement>()

        for(dataElememt in programDataElements){
            var element = TrackerController.getDataElement(dataElememt.dataelement)
            if(element != null){
                if(!element.displayName.contains("Show All")) {
                    assignedDataElements.add(element)
                }
            }
        }

        return assignedDataElements
    }

    fun getListDataElementForSession(
        orgUnitId: String,
        programId: String,
        sessionDate: String,
        trackedEntityInstances: List<TrackedEntityInstance>,
        assignedDataElements: List<DataElement>) : List<DataElement>{
        var sessionDataElements = arrayListOf<DataElement>()

        for(trackedEntityInstance in trackedEntityInstances){

            var vaccineList = arrayListOf<Vaccine>()
            var injectedVaccineList = arrayListOf<Vaccine>()
            var scheduleVaccineList = arrayListOf<Vaccine>()

            for(dataElement in assignedDataElements){
                vaccineList.add(Vaccine(dataElement, null,"", false))
            }

            var enrollment = TrackerController.getEnrollment(programId, trackedEntityInstance)

            if(enrollment != null){
                var events = enrollment.getEvents(true)
                if(events != null){
                    for(event in events){
                        var dataValues = event.dataValues
                        if(dataValues != null){
                            for(dataValue in dataValues){
                                for(vaccine in vaccineList){
                                    if(dataValue.dataElement.equals(vaccine.dataElement.uid)){
                                        vaccine.isInjected = if(dataValue.value.equals("true")) true else false
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                for(vaccine in vaccineList){
                    if(vaccine.isInjected)
                        injectedVaccineList.add(vaccine)
                }

                scheduleVaccineList = MethodUtils.createScheduleVaccineList(
                    LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(enrollment.incidentDate)),
                    LocalDate.now(),
                    //LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(sessionDate)).plusDays(3),
                    injectedVaccineList,
                    vaccineList
                )

                var outSessionVaccineList = arrayListOf<Vaccine>()
                for(vaccine in scheduleVaccineList){
                    if(!isVaccineDueDateOnSession(sessionDate, vaccine.dueDate))
                        outSessionVaccineList.add(vaccine)
                }

                scheduleVaccineList.removeAll(outSessionVaccineList)

                for(vaccine in scheduleVaccineList){
                    var hasElementOnSessionList = false
                    for(dataElement in sessionDataElements){
                        if(vaccine.dataElement.displayName.equals(dataElement.displayName)) {
                            hasElementOnSessionList = true
                            break
                        }
                    }

                    if(!hasElementOnSessionList)
                        sessionDataElements.add(vaccine.dataElement)
                }
            }
        }

        return sessionDataElements
    }

    fun getSessionWiseDataList(
        orgUnitId: String,
        programId: String,
        sessionDate: String,
        trackedEntityInstances: List<TrackedEntityInstance>,
        sessionDataElements: List<DataElement>,
        totalDataElements: List<DataElement>) : List<ImmunisationCard>{

        var immunisationCardList = arrayListOf<ImmunisationCard>()

        for(trackedEntityInstance in trackedEntityInstances){

            var immunisationCard = ImmunisationCard()
            var vaccineList = arrayListOf<Vaccine>()
            var sessionVaccineList = arrayListOf<Vaccine>()
            var injectedVaccineList = arrayListOf<Vaccine>()
            var scheduleVaccineList = arrayListOf<Vaccine>()

            for(dataElement in totalDataElements){
                vaccineList.add(Vaccine(dataElement, null,"", false))
            }

            for(dataElement in sessionDataElements){
                sessionVaccineList.add(Vaccine(dataElement, null,"", false))
            }

            var enrollment = TrackerController.getEnrollment(programId, trackedEntityInstance)

            if(enrollment != null){

                var events = enrollment.getEvents(true)
                if(events != null){
                    for(event in events){
                        var dataValues = event.dataValues
                        if(dataValues != null){
                            for(dataValue in dataValues){
                                for(vaccine in vaccineList){
                                    if(dataValue.dataElement.equals(vaccine.dataElement.uid)){
                                        vaccine.isInjected = if(dataValue.value.equals("true")) true else false
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                for(vaccine in vaccineList){
                    if(vaccine.isInjected)
                        injectedVaccineList.add(vaccine)
                }

                scheduleVaccineList = MethodUtils.createScheduleVaccineList(
                    LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(enrollment.incidentDate)),
                    //LocalDate.now(),
                    LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(sessionDate)).plusDays(3),
                    injectedVaccineList,
                    sessionVaccineList
                )

                scheduleVaccineList.removeAll(injectedVaccineList)

                for(scheduleVaccine in scheduleVaccineList){
                    for(vaccine in sessionVaccineList){
                        if(vaccine.dataElement.uid.equals(scheduleVaccine.dataElement.uid)){
                            vaccine.isInjected = true
                            break
                        }
                    }
                }

                immunisationCard.enrollment = enrollment
                immunisationCard.trackedEntityInstance = trackedEntityInstance
                immunisationCard.vaccineList = sessionVaccineList

                //var isHasVaccineOnSession = false
                for(vaccine in sessionVaccineList){
                    if(vaccine.isInjected) {
                        immunisationCardList.add(immunisationCard)
                        break
                    }
                }
            }
        }

        return immunisationCardList
    }

    fun getTotalDoseList(
        orgUnitId: String,
        programId: String,
        sessionDataElements: List<DataElement>,
        immunisationCardList: List<ImmunisationCard>) : List<Dose>{
        var totalDose = arrayListOf<Dose>()
        for(sessionElement in sessionDataElements){
            var doseCount = 0
            for(immunisationCard in immunisationCardList){
                for(vaccine in immunisationCard.vaccineList){
                    if(vaccine.dataElement.uid.equals(sessionElement.uid)
                        && vaccine.isInjected){
                            doseCount++
                            break
                    }
                }
            }
            totalDose.add(Dose(sessionElement.uid, doseCount))
        }
        return totalDose
    }

    fun isVaccineDueDateOnSession(sessionDate: String, dueDate: String) : Boolean{
        var sessionLocalDate = LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(sessionDate))
        var dueLocalDate = LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(dueDate))
        if(sessionLocalDate.isAfter(dueLocalDate)
            || sessionLocalDate.isEqual(dueLocalDate)
            || (dueLocalDate.isAfter(sessionLocalDate) && Days.daysBetween(sessionLocalDate, dueLocalDate).days <= 3)){
            return true
        }

        return false
    }

    // -------END - SESSION WISE SUB-MODULE ----------- //



    // SURVEY MODULE
    fun getSurveyEntities(orgUnitId: String, programId: String){
        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {
                    var events = listOf<Event>()

                    events = TrackerController.getEventEntries(orgUnitId, programId)

                    if(isViewAttached)
                        view.getSurveyEvents(events)

                    return Any()
                }
            })
        }catch(exception : APIException){
            if(isViewAttached)
                view.getApiFailed(exception)
        }
    }

    fun getWorkplanEntities(orgUnitId: String, programId: String){
        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {
                    var events = listOf<Event>()

                    events = TrackerController.getEventEntries(orgUnitId, programId)

                    if(isViewAttached)
                        view.getWorkplanEvents(events)

                    return Any()
                }
            })
        }catch(exception : APIException){
            if(isViewAttached)
                view.getApiFailed(exception)
        }
    }
}
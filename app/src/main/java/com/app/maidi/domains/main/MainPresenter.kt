package com.app.maidi.domains.main

import android.util.Log
import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.models.Dose
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.models.Vaccine
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import com.app.maidi.utils.Constants
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import io.reactivex.disposables.Disposable
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.job.JobExecutor
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.ResponseHolder
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.Enrollment
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
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

    fun getRemoteAefiTrackedEntityInstances(orgUnitId: String, programId: String){
        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {

                    var trackedEntityInstances = listOf<TrackedEntityInstance>()

                    trackedEntityInstances = TrackerController.queryTrackedEntityInstancesDataFromServer(
                        DhisController.getInstance().dhisApi,
                        orgUnitId,
                        programId, ""
                    )

                    for(trackedEntityInstance in trackedEntityInstances){

                        TrackerController.getEnrollmentDataFromServer(
                            DhisController.getInstance().dhisApi,
                            trackedEntityInstance,
                            null)
                    }

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

    fun getRemoteTrackedEntityInstances(orgUnitId: String, programId: String){

        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {

                    var immunisationCardList = arrayListOf<ImmunisationCard>()
                    var trackedEntityInstances = listOf<TrackedEntityInstance>()

                    trackedEntityInstances = TrackerController.queryTrackedEntityInstancesDataFromServer(
                        DhisController.getInstance().dhisApi,
                        orgUnitId,
                        programId, ""
                    )

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

                        for(dataElement in assignedDataElements){
                            vaccineList.add(Vaccine(dataElement, "", false))
                        }

                        var enrollments = TrackerController.getEnrollmentDataFromServer(
                            DhisController.getInstance().dhisApi,
                            trackedEntityInstance,
                            null)

                        for(enrollment in enrollments){
                            if(enrollment.trackedEntityInstance.equals(trackedEntityInstance.uid)){
                                var events = enrollment.getEvents(true)
                                if(events != null){
                                    for(event in events){
                                        var dataValues = event.dataValues
                                        if(dataValues != null){
                                            for(dataValue in dataValues){
                                                for(vaccine in vaccineList){
                                                    if(dataValue.dataElement.equals(vaccine.dataElement.uid)){
                                                        vaccine.dueDate = event.dueDate
                                                        vaccine.isInjected = true
                                                        break
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }

                                immunisationCard.enrollment = enrollment
                            }

                        }

                        immunisationCard.trackedEntityInstance = trackedEntityInstance
                        immunisationCard.vaccineList = vaccineList//.subList(0, 6)

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

    fun getSessionWiseDatas(orgUnitId: String, programId: String){
        if(isViewAttached){
            view.showLoading()
        }

        try{
            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {

                    var dataElements = getProgramDataElement(programId)
                    if(isViewAttached)
                        view.getProgramDataElements(dataElements)

                    var immunisationCardList = getSessionWiseDataList(orgUnitId, programId)
                    if(isViewAttached)
                        view.getSessionWiseDataListSuccess(immunisationCardList)

                    var doseList = arrayListOf<Dose>()
                    for(element in dataElements){
                        var total = getTotalDosesForVaccine(element)
                        doseList.add(Dose(element.uid, total))
                    }

                    if(isViewAttached) {
                        view.getTotalDoseList(doseList)
                        view.hideLoading()
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

    fun getSessionWiseDataList(orgUnitId: String, programId: String) : List<ImmunisationCard>{

        var immunisationCardList = arrayListOf<ImmunisationCard>()
        var trackedEntityInstances = listOf<TrackedEntityInstance>()

        trackedEntityInstances = TrackerController.queryTrackedEntityInstancesDataFromServer(
            DhisController.getInstance().dhisApi,
            orgUnitId,
            programId, ""
        )


        var assignedDataElements = getProgramDataElement(programId)

        for(trackedEntityInstance in trackedEntityInstances){

            var immunisationCard = ImmunisationCard()
            var vaccineList = arrayListOf<Vaccine>()

            for(dataElement in assignedDataElements){
                vaccineList.add(Vaccine(dataElement, "", false))
            }

            var enrollments = TrackerController.getEnrollmentDataFromServer(
                DhisController.getInstance().dhisApi,
                trackedEntityInstance,
                null)

            for(enrollment in enrollments){
                if(enrollment.trackedEntityInstance.equals(trackedEntityInstance.uid)){
                    var events = enrollment.getEvents(true)
                    if(events != null){
                        for(event in events){
                            var dataValues = event.dataValues
                            if(dataValues != null){
                                for(dataValue in dataValues){
                                    for(vaccine in vaccineList){
                                        if(dataValue.dataElement.equals(vaccine.dataElement.uid)){
                                            vaccine.dueDate = event.dueDate
                                            vaccine.isInjected = true
                                            break
                                        }
                                    }

                                }
                            }
                        }
                    }

                    immunisationCard.enrollment = enrollment
                }

            }

            immunisationCard.trackedEntityInstance = trackedEntityInstance
            immunisationCard.vaccineList = vaccineList

            immunisationCardList.add(immunisationCard)

        }

        return immunisationCardList
    }

    fun getTotalDosesForVaccine(dataElement: DataElement) : Int{
        var doseLists = TrackerController.getDataValuesFollowElement(dataElement.uid)
        if(doseLists != null)
            return doseLists.size

        return 0
    }
}
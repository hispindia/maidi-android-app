package com.app.maidi.domains.my_registration.immunisation_detail

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.models.Vaccine
import com.app.maidi.utils.Constants
import com.app.maidi.utils.MethodUtils
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.job.JobExecutor
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.ResponseHolder
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import org.joda.time.LocalDate
import javax.inject.Inject

class ImmunisationDetailPresenter : BasePresenter<ImmunisationDetailView> {

    @Inject
    constructor(){}

    fun queryImmunisationInfo(organUnitId: String, programId: String, trackedEntityInstance: TrackedEntityInstance){

        if(isViewAttached)
            view.showLoading()

        try{

            JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

                @Throws(APIException::class)
                override fun execute(): Any {

                        var vaccineList = arrayListOf<Vaccine>()
                        var injectedVaccineList = arrayListOf<Vaccine>()

                        TrackerController.getEnrollmentDataFromServer(DhisController.getInstance().dhisApi, trackedEntityInstance, null)

                        var events : List<Event> = TrackerController.getEvents(organUnitId, programId, trackedEntityInstance.uid)

                        var programStage = TrackerController.getProgramStageByName(programId, Constants.IMMUNISATION)
                        var programDataElements = TrackerController.getProgramStageDataElements(programStage.uid)
                        var enrollment = TrackerController.getEnrollment(programId, trackedEntityInstance)
                        //var programStages = TrackerController.getProgramStages(programId)
                        //for(stage in programStages){
                            //var dataElements = TrackerController.getProgramStageDataElements(stage.uid)
                            for(dataElement in programDataElements){
                                var element = TrackerController.getDataElement(dataElement.dataelement)
                                if(!element.displayName.contains("Show")) {
                                    vaccineList.add(Vaccine(element, null, "", false))
                                }
                            }
                        //}

                        for(event in events){
                            var dataValues = TrackerController.getDataValue(event.uid)
                            for (vaccine in vaccineList) {
                                for(dataValue in dataValues){
                                    var dataElement = TrackerController.getDataElement(dataValue.dataElement)
                                    if (vaccine.dataElement.uid.equals(dataElement.uid)) {
                                        vaccine.isInjected = if(dataValue.value.equals("true")) true else false
                                        vaccine.dueDate = event.dueDate
                                        vaccine.isShowed = true
                                        if(dataValue.value.equals("true")){
                                            injectedVaccineList.add(vaccine)
                                        }
                                        //isHasValue = true
                                    }
                                }

                                /*if(!isHasValue){
                                    if (event.eventDate != null && !event.eventDate.isEmpty()) {
                                        if (DateUtils.checkVaccineReachDueDate(
                                                vaccine.dataElement.displayName,
                                                LocalDate(DateUtils.parseDate(enrollment.incidentDate)),
                                                LocalDate(DateUtils.parseDate(event.eventDate))
                                            )
                                        ) {
                                            vaccine.dueDate = event.dueDate
                                            vaccine.isShowed = true
                                        }
                                    }else{
                                        if (DateUtils.checkVaccineReachDueDate(
                                                vaccine.dataElement.displayName,
                                                LocalDate(DateUtils.parseDate(enrollment.incidentDate)),
                                                LocalDate.now()
                                            )
                                        ) {
                                            vaccine.dueDate = event.dueDate
                                            vaccine.isShowed = true
                                        }
                                    }
                                }*/
                            }
                        }

                        var scheduleVaccineList = MethodUtils.createScheduleVaccineList(
                            LocalDate(org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(enrollment.incidentDate)),
                            LocalDate.now(),
                            injectedVaccineList,
                            vaccineList
                        )

                        /*var filtedVaccineList = arrayListOf<Vaccine>()
                        for(vaccine in vaccineList){
                            if(vaccine.isShowed){
                                filtedVaccineList.add(vaccine)
                            }
                        }*/

                        if(isViewAttached) {
                            view.getDataElementSuccess(scheduleVaccineList)
                        }

                    return Any()
                }
            })

        }catch (exception : APIException){
            if(isViewAttached) {
                view.getDataElementFailed(exception)
                view.hideLoading()
            }
        }
    }

    fun queryLocalImmunisationInfo(programId: String){

        var immunisationInfos = arrayListOf<Vaccine>()

        var programStages = TrackerController.getProgramStages(programId)
        for(stage in programStages){
            var dataElements = TrackerController.getProgramStageDataElements(stage.uid)
            for(dataElement in dataElements){
                var element = TrackerController.getDataElement(dataElement.dataelement)
                if(!element.displayName.contains("Birth")) {
                    immunisationInfos.add(Vaccine(element, null,"", false))
                }
            }
        }

        if(isViewAttached) {
            view.getDataElementSuccess(immunisationInfos)
        }
    }
}
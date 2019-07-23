package com.app.maidi.domains.my_registration.immunisation_detail

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.models.Vaccine
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.job.JobExecutor
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.ResponseHolder
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
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

                        TrackerController.getEnrollmentDataFromServer(DhisController.getInstance().dhisApi, trackedEntityInstance, null)

                        var events : List<Event> = TrackerController.getEvents(organUnitId, programId, trackedEntityInstance.uid)

                        var programStages = TrackerController.getProgramStages(programId)
                        for(stage in programStages){
                            var dataElements = TrackerController.getProgramStageDataElements(stage.uid)
                            for(dataElement in dataElements){
                                var element = TrackerController.getDataElement(dataElement.dataelement)
                                vaccineList.add(Vaccine(element, "", false))
                            }
                        }

                        for(event in events){
                            var dataValues = TrackerController.getDataValue(event.uid)

                            for(dataValue in dataValues){
                                var dataElement = TrackerController.getDataElement(dataValue.dataElement)
                                for(vaccine in vaccineList){
                                    if(vaccine.dataElement.uid.equals(dataElement.uid)){
                                        vaccine.isInjected = true
                                        vaccine.dueDate = event.dueDate
                                    }
                                }
                            }
                        }

                        if(isViewAttached) {
                            view.getDataElementSuccess(vaccineList)
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
                immunisationInfos.add(Vaccine(element, "", false))
            }
        }

        if(isViewAttached) {
            view.getDataElementSuccess(immunisationInfos)
        }
    }
}
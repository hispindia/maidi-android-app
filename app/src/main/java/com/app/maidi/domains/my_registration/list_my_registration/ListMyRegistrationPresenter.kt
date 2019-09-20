package com.app.maidi.domains.my_registration.list_my_registration

import com.app.maidi.domains.base.BasePresenter
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.job.JobExecutor
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.ResponseHolder
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import org.hisp.dhis.android.sdk.utils.support.DateUtils
import org.joda.time.LocalDate
import javax.inject.Inject

class ListMyRegistrationPresenter : BasePresenter<ListMyRegistrationView> {

    @Inject
    constructor(){}

    fun queryListMyRegistration(orgUnitId: String, programId: String, birthday: String, phoneValue: TrackedEntityAttributeValue){

        if(isViewAttached)
            view.showLoading()

        JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

            @Throws(APIException::class)
            override fun execute(): Any {

                try{
                    var birthDate = LocalDate(DateUtils.parseDate(birthday))
                    var filterSearchResultList = arrayListOf<TrackedEntityInstance>()
                    var dhisApi = DhisController.getInstance().dhisApi
                    var searchPhoneResultInstances =
                            TrackerController.queryTrackedEntityInstancesDataFromAllAccessibleOrgUnits(
                                dhisApi, orgUnitId, programId, "", false, phoneValue
                            )

                        for(instance in searchPhoneResultInstances){
                            var enrollments = TrackerController.getEnrollmentDataFromServer(dhisApi, instance, null)
                            if(enrollments != null){
                                for(enrollment in enrollments){
                                    if(enrollment.trackedEntityInstance.equals(instance.trackedEntityInstance)) {
                                        var incidentDate = LocalDate(DateUtils.parseDate(enrollment.incidentDate))
                                        if (incidentDate.isEqual(birthDate)) {
                                            filterSearchResultList.add(instance)
                                            break
                                        }
                                    }
                                }
                            }
                        }

                    if(isViewAttached)
                        view.getListMyRegistrationSuccess(filterSearchResultList)

                }catch (ex : APIException){
                    if(isViewAttached)
                        view.getListMyRegistrationFailed(ex)
                }

                if(isViewAttached)
                    view.hideLoading()

                return Any()
            }
        })
    }
}
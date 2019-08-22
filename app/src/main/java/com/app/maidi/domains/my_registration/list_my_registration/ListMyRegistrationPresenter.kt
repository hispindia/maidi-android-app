package com.app.maidi.domains.my_registration.list_my_registration

import com.app.maidi.domains.base.BasePresenter
import com.app.maidi.networks.NetworkProvider
import com.app.maidi.services.account.AccountService
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.job.JobExecutor
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.ResponseHolder
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttribute
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import javax.inject.Inject

class ListMyRegistrationPresenter : BasePresenter<ListMyRegistrationView> {

    @Inject
    constructor(){}

    fun queryListMyRegistration(orgUnitId: String, programId: String, queryString : String, detailedSearch : Boolean, birthdayValue: TrackedEntityAttributeValue, phoneValue: TrackedEntityAttributeValue){

        if(isViewAttached)
            view.showLoading()

        JobExecutor.enqueueJob<ResponseHolder<Any>>(object : NetworkJob<Any>(1, null) {

            @Throws(APIException::class)
            override fun execute(): Any {

                try{
                    /*var searchTrackedEntityInstanceLists = ArrayList<TrackedEntityInstance>()
                    var trackedEntityInstancesQueryResult: List<TrackedEntityInstance>?
                    if (detailedSearch) {
                        trackedEntityInstancesQueryResult =
                            TrackerController.queryTrackedEntityInstancesDataFromAllAccessibleOrgUnits(
                                DhisController.getInstance().dhisApi, orgUnitId, programId, queryString, detailedSearch, birthdayValue, phoneValue
                            )
                    } else {
                        trackedEntityInstancesQueryResult = TrackerController.queryTrackedEntityInstancesDataFromServer(
                            DhisController.getInstance().dhisApi,
                            orgUnitId,
                            programId,
                            queryString,
                            birthdayValue,
                            phoneValue
                        )
                    }*/

                    var trackedEntityInstancesQueryResult = TrackerController.queryLocalTrackedEntityInstances(birthdayValue, phoneValue)

                    /*if(trackedEntityInstancesQueryResult != null){
                        searchTrackedEntityInstanceLists.addAll(trackedEntityInstancesQueryResult)
                    }

                    if(localTrackedEntityInstances != null){
                        searchTrackedEntityInstanceLists.addAll(localTrackedEntityInstances)
                    }*/

                    if(isViewAttached)
                        //view.getListMyRegistrationSuccess(searchTrackedEntityInstanceLists)
                        view.getListMyRegistrationSuccess(trackedEntityInstancesQueryResult)
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
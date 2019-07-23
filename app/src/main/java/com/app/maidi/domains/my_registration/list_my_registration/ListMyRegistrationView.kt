package com.app.maidi.domains.my_registration.list_my_registration

import com.app.maidi.domains.base.BaseView
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import java.lang.Exception

interface ListMyRegistrationView : BaseView {

    fun getListMyRegistrationSuccess(trackedEntityInstances : List<TrackedEntityInstance>)
    fun getListMyRegistrationFailed(exception: APIException)
}
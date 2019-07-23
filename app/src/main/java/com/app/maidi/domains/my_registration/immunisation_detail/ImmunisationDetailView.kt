package com.app.maidi.domains.my_registration.immunisation_detail

import com.app.maidi.domains.base.BaseView
import com.app.maidi.models.Vaccine
import org.hisp.dhis.android.sdk.network.APIException
import java.lang.Exception

interface ImmunisationDetailView : BaseView {

    fun getDataElementSuccess(dataElement: ArrayList<Vaccine>)
    fun getDataElementFailed(exception: APIException)
}
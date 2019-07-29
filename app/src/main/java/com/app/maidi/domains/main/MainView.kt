package com.app.maidi.domains.main

import com.app.maidi.domains.base.BaseView
import com.app.maidi.models.ImmunisationCard
import com.hannesdorfmann.mosby3.mvp.MvpView
import org.hisp.dhis.android.sdk.network.APIException

interface MainView : BaseView {
    fun getImmunisationCardListSuccess(immunisationList: List<ImmunisationCard>)
    fun getSessionWiseDataListSuccess(sessionWiseList: List<ImmunisationCard>)
    fun getApiFailed(exception: APIException)
}
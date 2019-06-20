package com.app.maidi.services.account

import com.app.maidi.models.database.User
import com.app.maidi.networks.NetworkProvider
import io.reactivex.Observable
import okhttp3.Credentials
import javax.inject.Inject

class DefaultAccountService : AccountService{

    private var networkProvider: NetworkProvider
    private var restServiceApi: AccountServiceApi

    @Inject
    constructor(networkProvider: NetworkProvider, restServiceApi: AccountServiceApi){
        this.networkProvider = networkProvider
        this.restServiceApi = restServiceApi
    }

    override fun login(username: String, password: String): Observable<User> {
        val QUERY_PARAMS = HashMap<String, String>()
        QUERY_PARAMS["fields"] = "id,created,lastUpdated,name,displayName," +
                "firstName,surname,gender,birthday,introduction," +
                "education,employer,interests,jobTitle,languages,email,phoneNumber," +
                "teiSearchOrganisationUnits[id],organisationUnits[id],programs"
        var base64Credentials = Credentials.basic(username, password)
        //networkProvider.setHeader("Authorization", base64Credentials)
        return networkProvider.transformResponse(restServiceApi.getCurrentUserInfo(base64Credentials, QUERY_PARAMS))
            .flatMap { t: User? -> Observable.just(t) }
    }
}
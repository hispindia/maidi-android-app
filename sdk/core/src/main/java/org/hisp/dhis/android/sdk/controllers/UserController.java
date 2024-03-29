/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.sdk.controllers;

import android.util.Log;
import com.raizlabs.android.dbflow.sql.language.Delete;
import okhttp3.HttpUrl;
import org.hisp.dhis.android.sdk.network.*;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.persistence.preferences.DateTimeManager;
import org.hisp.dhis.android.sdk.persistence.preferences.LastUpdatedManager;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Araz Abishov <araz.abishov.gsoc@gmail.com>.
 */
final class UserController {
    private final DhisApi dhisApi;

    public UserController(DhisApi dhisApi) {
        this.dhisApi = dhisApi;
    }

    public UserAccount logInUser(HttpUrl serverUrl, Credentials credentials) throws APIException {
        final Map<String, String> QUERY_PARAMS = new HashMap<>();

        UserAccount userAccount = null;

        QUERY_PARAMS.put("fields", "id,created,lastUpdated,name,displayName," +
                "firstName,surname,gender,birthday,introduction," +
                "education,employer,interests,jobTitle,languages,email,phoneNumber," +
                "teiSearchOrganisationUnits[id],organisationUnits[id], userCredentials");
        try {
            Response<UserAccount> response = dhisApi.getCurrentUserAccount(QUERY_PARAMS).execute();
            Log.d("", "");
            userAccount = response.body();
        }catch (IOException ex) {
            Log.e("Exception", ex.toString());
        }

        Session session = new Session(serverUrl, credentials);
        LastUpdatedManager.getInstance().put(session);

        /* save user account details */
        userAccount.save();

        return userAccount;
    }

    public void logOut() {
        LastUpdatedManager.getInstance().delete();
        DateTimeManager.getInstance().delete();
        SessionManager.getInstance().delete();

        // remove data todo add more
        Delete.tables(
                UserAccount.class
        );
    }

    //ADD NEW FUNCTIONS - 2019
    public UserAccount logInUserWithPhoneNumber(HttpUrl serverUrl, Credentials credentials, String phoneNumber) throws APIException {
        final Map<String, String> QUERY_PARAMS = new HashMap<>();
        QUERY_PARAMS.put("fields", "id,created,lastUpdated,name,displayName," +
                "firstName,surname,gender,birthday,introduction," +
                "education,employer,interests,jobTitle,languages,email,phoneNumber," +
                "teiSearchOrganisationUnits[id],organisationUnits[id], userCredentials");

        UserAccount userAccount = new UserAccount();

        try {
            userAccount = dhisApi.getCurrentUserAccount(QUERY_PARAMS).execute().body();
            Session session = new Session(serverUrl, credentials);
            LastUpdatedManager.getInstance().put(session);

            //Store phone number into user account's info
            userAccount.setPhoneNumber(phoneNumber);

            /* save user account details */
            userAccount.save();
        }catch (IOException ex) {
            Log.e("Exception", ex.toString());
        }

        /*UserAccount userAccount = dhisApi
                .getCurrentUserAccount(QUERY_PARAMS);

        // if we got here, it means http
        // request was executed successfully

        *//* save user credentials *//*
        Session session = new Session(serverUrl, credentials);
        LastUpdatedManager.getInstance().put(session);

        *//* save user account details *//*
        userAccount.save();
*/
        return userAccount;
    }
}

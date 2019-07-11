package com.app.maidi.utils

import com.app.maidi.BuildConfig

class Constants{
    companion object{
        //var DHIS2_SERVER_URL = "http://192.168.1.254:8080/dhis/"
        val DHIS2_SERVER_URL = if(BuildConfig.FLAVOR.equals("staging")) "https://maidi.icmr.org.in/dhis/" else "http://1ca68901.ngrok.io/dhis/" //"http://192.168.0.103:8080/dhis/"
        val GUEST_ROLE = if(BuildConfig.FLAVOR.equals("staging")) "Guest role" else "guest"
        val PHONE_NUMBER = "PHONE_NUMBER"
        val VERIFICATION_ID = "VERIFICATION_ID"
        val RESEND_TOKEN = "RESEND_TOKEN"

        val INDIA_PHONE_NUMBER_REGEX = "^[6-9]\\d{9}\$"
        val PHONE_NUMBER_PREFIX = "+91"
    }
}
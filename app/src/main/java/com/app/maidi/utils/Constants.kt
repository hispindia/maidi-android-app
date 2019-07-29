package com.app.maidi.utils

import com.app.maidi.BuildConfig
import java.text.SimpleDateFormat

class Constants{
    companion object{
        //var DHIS2_SERVER_URL = "http://192.168.1.254:8080/dhis/"

        val SIMPLE_DATE_PATTERN = "MM/dd/yyyy"
        val FULL_DATE_PATTERN = "yyyy-MM-dd'T'hh:mm:ss.SSS"
        val SERVER_DATE_PATTERN = "yyyy-MM-dd"

        val DHIS2_SERVER_URL =
            if(BuildConfig.FLAVOR.equals("staging")) "https://maidi.icmr.org.in/dhis/"
            else if(BuildConfig.FLAVOR.equals("production")) "http://32eec65c.ngrok.io/dhis/"
            else "http://192.168.0.105:8080/dhis/"
        val BENEFICIARY_CHILD_REGISTRATION = "Child registration"
        val IMMUNISATION = "Immunisation"
        val GUEST_ROLE = "Guest role"
        val PHONE_NUMBER = "PHONE_NUMBER"
        val VERIFICATION_ID = "VERIFICATION_ID"
        val RESEND_TOKEN = "RESEND_TOKEN"

        val INDIA_PHONE_NUMBER_REGEX = "^[6-9]\\d{9}\$"
        val PHONE_NUMBER_PREFIX = "+91"
    }
}
package com.app.maidi.utils

import com.app.maidi.BuildConfig
import java.text.SimpleDateFormat

class Constants{
    companion object{
        //var DHIS2_SERVER_URL = "http://192.168.1.254:8080/dhis/"

        val EXPORT_FOLDER_NAME = "Maidi_Export"
        val SIMPLE_MONTH_YEAR_PATTERN = "MM/yyyy"
        val SIMPLE_DATE_PATTERN = "MM/dd/yyyy"
        val SIMPLE_SERVER_DATE_PATTERN = "yyyy-MM-dd"
        val FULL_DATE_PATTERN = "yyyy-MM-dd'T'hh:mm:ss.SSS"
        val SIMPLE_DATE_WITH_DAY_OF_WEEK_PATTERN = "EEE, dd MMM yyyy"
        val SERVER_DATE_PATTERN = "^(([^01][0-9]|19|[2-9][0-9])\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])\$"

        val DHIS2_SERVER_URL =
            if(BuildConfig.FLAVOR.equals("staging")) "https://maidi.icmr.org.in/dhis/api/"
            else if(BuildConfig.FLAVOR.equals("production")) "http://eeb52f31.ngrok.io/dhis/api/"
            else "http://192.168.1.231:8080/dhis/api/"

        val BENEFICIARY_CHILD_REGISTRATION = "Child registration"
        val IMMUNISATION = "Immunisation"
        val AEFI = "AEFI"
        var SURVEY = "survey"
        var WORKPLAN = "workplan"
        val GUEST_ROLE = "Guest role"
        val PHONE_NUMBER = "PHONE_NUMBER"
        val VERIFICATION_ID = "VERIFICATION_ID"
        val RESEND_TOKEN = "RESEND_TOKEN"

        val INDIA_PHONE_NUMBER_REGEX = "^[6-9]\\d{9}\$"
        val PHONE_NUMBER_PREFIX = "+91"
        //val PHONE_NUMBER_PREFIX = "+84"
    }
}
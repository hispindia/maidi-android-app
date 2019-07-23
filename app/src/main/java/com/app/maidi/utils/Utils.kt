package com.app.maidi.utils

import android.content.res.AssetManager
import android.util.Base64
import android.util.Log
import android.util.Patterns
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class Utils {

    companion object{

        val simpleDateFormat = SimpleDateFormat(Constants.SIMPLE_DATE_PATTERN)
        val fullDateFormat = SimpleDateFormat(Constants.FULL_DATE_PATTERN)
        val serverDateFormat = SimpleDateFormat(Constants.SERVER_DATE_PATTERN)

        fun convertLocalDateToServerDate(localDateString: String) : String{
            var localDate = simpleDateFormat.parse(localDateString)
            val serverDateString = serverDateFormat.format(localDate)
            return serverDateString
        }

        fun convertStringToCalendar(dateString: String) : Calendar{
            var date = simpleDateFormat.parse(dateString)
            var cal = Calendar.getInstance()
            cal.time = date
            return cal
        }

        fun convertStringToLocalDate(dateString: String) : LocalDate{
            return LocalDate.parse(dateString, DateTimeFormat.forPattern(Constants.SIMPLE_DATE_PATTERN))
        }

        fun convertCalendarToString(date: Date) : String{
            var dateString = simpleDateFormat.format(date)
            return dateString
        }

        fun isValidPhoneNumber(phoneNumber : String) : Boolean{
            return Pattern.matches(Constants.INDIA_PHONE_NUMBER_REGEX, phoneNumber)
        }

        fun convertFromFullDateToSimpleDate(dateString: String) : String{
            try {
                if (!dateString.isEmpty()) {
                    var dDate = fullDateFormat.parse(dateString)
                    var convertDate = simpleDateFormat.format(dDate)
                    return convertDate
                }
            }catch(ex : Exception){
                Log.d("Parse Exception", ex.toString())
            }
            return ""
        }

        fun isValidDateFollowPattern(regex: String, date: String) : Boolean{
            try {
                SimpleDateFormat(regex).parse(date)
                return true
            } catch (e: ParseException) {
                return false
            }
        }
    }

}
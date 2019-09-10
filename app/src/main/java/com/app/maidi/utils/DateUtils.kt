package com.app.maidi.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.AssetManager
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.textfield.TextInputEditText
import org.joda.time.*
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
import java.time.ZoneId
import java.util.*
import java.util.regex.Pattern


class DateUtils {

    companion object{

        val simpleLocalDateFormat = SimpleDateFormat(Constants.SIMPLE_DATE_PATTERN)
        val simpleMonthYearFormat = SimpleDateFormat(Constants.SIMPLE_MONTH_YEAR_PATTERN)
        val simpleServerDateFormat = SimpleDateFormat(Constants.SIMPLE_SERVER_DATE_PATTERN)
        val simpleDateWithDayOfWeek = SimpleDateFormat(Constants.SIMPLE_DATE_WITH_DAY_OF_WEEK_PATTERN)
        val fullDateFormat = SimpleDateFormat(Constants.FULL_DATE_PATTERN)
        val serverDateFormat = SimpleDateFormat(Constants.SERVER_DATE_PATTERN)

        fun convertLocalDateToServerDate(localDateString: String) : String{
            var localDate = simpleLocalDateFormat.parse(localDateString)
            val serverDateString = simpleServerDateFormat.format(localDate)
            return serverDateString
        }

        fun convertDayOfWeekDateToServerDate(localDateString: String) : String{
            var localDate = simpleDateWithDayOfWeek.parse(localDateString)
            val serverDateString = simpleServerDateFormat.format(localDate)
            return serverDateString
        }

        fun convertServerDateToLocalDate(serverDateString: String) : String{
            var serverDate = simpleServerDateFormat.parse(serverDateString)
            val localDate = simpleLocalDateFormat.format(serverDate)
            return localDate
        }

        fun convertMonthStringToCalendar(monthString: String) : Calendar{
            var date = simpleMonthYearFormat.parse(monthString)
            var cal = Calendar.getInstance()
            cal.time = date
            return cal
        }

        fun convertMonthStringToLocalDate(dateString: String) : LocalDate{
            return LocalDate.parse(dateString, DateTimeFormat.forPattern(Constants.SIMPLE_MONTH_YEAR_PATTERN))
        }

        fun convertDayWeekStringToCalendar(dateString: String): LocalDate{
            return LocalDate.parse(dateString, DateTimeFormat.forPattern(Constants.SIMPLE_DATE_WITH_DAY_OF_WEEK_PATTERN))
        }

        fun convertStringToCalendar(dateString: String) : Calendar{
            var date = simpleLocalDateFormat.parse(dateString)
            var cal = Calendar.getInstance()
            cal.time = date
            return cal
        }

        fun convertStringToLocalDate(dateString: String) : LocalDate{
            return LocalDate.parse(dateString, DateTimeFormat.forPattern(Constants.SIMPLE_DATE_PATTERN))
        }

        fun convertCalendarToMonthString(date: Date) : String {
            var dateString = simpleMonthYearFormat.format(date)
            return dateString
        }

        fun convertCalendarToString(date: Date) : String{
            var dateString = simpleLocalDateFormat.format(date)
            return dateString
        }

        fun convertCalendarToDayOfWeekString(date: Date) : String{
            var dateString = simpleDateWithDayOfWeek.format(date)
            return dateString
        }

        fun convertCalendarToServerString(date: Date) : String {
            var dateString = simpleServerDateFormat.format(date)
            return dateString
        }

        fun isValidPhoneNumber(phoneNumber : String) : Boolean{
            return Pattern.matches(Constants.INDIA_PHONE_NUMBER_REGEX, phoneNumber)
        }

        fun convertFromFullDateToSimpleDate(dateString: String) : String{
            try {
                if (!dateString.isEmpty()) {
                    var dDate = fullDateFormat.parse(dateString)
                    var convertDate = simpleLocalDateFormat.format(dDate)
                    return convertDate
                }
            }catch(ex : Exception){
                Log.d("Parse Exception", ex.toString())
            }
            return ""
        }

        fun isValidDateFollowPattern(date: String) : Boolean{
            try {
                return Pattern.matches(Constants.SERVER_DATE_PATTERN, date)
            } catch (e: ParseException) {
                return false
            }
        }
    }

}
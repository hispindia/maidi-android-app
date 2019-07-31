package com.app.maidi.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.res.AssetManager
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.textfield.TextInputEditText
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
                return Pattern.matches(Constants.SERVER_DATE_PATTERN, date)
            } catch (e: ParseException) {
                return false
            }
        }

        fun showHideContainer(containerView: View, duration: Int){
            val expand = containerView.visibility != View.VISIBLE
            val prevHeight = containerView.height
            var height = 0
            if (expand) {
                val measureSpecParams = View.MeasureSpec.getSize(View.MeasureSpec.UNSPECIFIED)
                containerView.measure(measureSpecParams, measureSpecParams)
                height = containerView.measuredHeight
            }

            val valueAnimator = ValueAnimator.ofInt(prevHeight, height)
            valueAnimator.addUpdateListener { animation ->
                containerView.layoutParams.height = animation.animatedValue as Int
                containerView.requestLayout()
            }

            valueAnimator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    if (!expand) {
                        containerView.visibility = View.INVISIBLE
                    }
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationStart(p0: Animator?) {
                    if (expand) {
                        containerView.visibility = View.VISIBLE
                    }
                }
            })
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration.toLong()
            valueAnimator.start()
        }

        fun showHideDateContainer(etDateOfBirth: TextInputEditText, datePicker: SingleDateAndTimePicker, duration: Int){
            val expand = datePicker.visibility != View.VISIBLE
            val prevHeight = datePicker.height
            var height = 0
            if (expand) {
                val measureSpecParams = View.MeasureSpec.getSize(View.MeasureSpec.UNSPECIFIED)
                datePicker.measure(measureSpecParams, measureSpecParams)
                height = datePicker.measuredHeight
            }

            val valueAnimator = ValueAnimator.ofInt(prevHeight, height)
            valueAnimator.addUpdateListener { animation ->
                datePicker.layoutParams.height = animation.animatedValue as Int
                datePicker.requestLayout()
            }

            valueAnimator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    if (!expand) {
                        datePicker.visibility = View.INVISIBLE
                        etDateOfBirth.setText(convertCalendarToString(datePicker.date))
                    }
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationStart(p0: Animator?) {
                    if (expand) {
                        datePicker.visibility = View.VISIBLE
                        datePicker.selectDate(convertStringToCalendar(etDateOfBirth.text.toString()))
                    }
                }
            })
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration.toLong()
            valueAnimator.start()
        }
    }

}
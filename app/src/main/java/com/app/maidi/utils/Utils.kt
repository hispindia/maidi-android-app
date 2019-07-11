package com.app.maidi.utils

import android.content.res.AssetManager
import android.util.Base64
import android.util.Patterns
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
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class Utils {

    companion object{

        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy")

        fun convertStringToCalendar(dateString: String) : Calendar{
            var date = simpleDateFormat.parse(dateString)
            var cal = Calendar.getInstance()
            cal.time = date
            return cal
        }

        fun convertCalendarToString(date: Date) : String{
            var dateString = simpleDateFormat.format(date)
            return dateString
        }

        fun isValidPhoneNumber(phoneNumber : String) : Boolean{
            return Pattern.matches(Constants.INDIA_PHONE_NUMBER_REGEX, phoneNumber)
        }
    }

}
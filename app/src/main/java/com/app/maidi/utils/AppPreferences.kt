package com.app.maidi.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity

class AppPreferences {

    private val APP_PREFERENCES_NAME = "maidi:preferences"
    private val SERVER_URL_KEY = "key:serverUrl"
    private val USERNAME_KEY = "key:userName"

    private lateinit var prefs : SharedPreferences

    constructor(context : FragmentActivity?){
        if(context != null){
            prefs = context.getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }

    fun putUsername(username: String){
        var editor = prefs.edit()

        if(prefs.contains(USERNAME_KEY)){
            editor.remove(USERNAME_KEY)
        }

        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    fun getUsername() : String {
        if(prefs.contains(USERNAME_KEY))
            return prefs.getString(USERNAME_KEY, null)
        return ""
    }

    fun clearData(){
        prefs.edit().remove(USERNAME_KEY)
    }
}
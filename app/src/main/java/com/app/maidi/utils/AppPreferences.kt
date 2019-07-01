package com.app.maidi.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreferences {

    private val APP_PREFERENCES_NAME = "maidi:preferences"
    private val SERVER_URL_KEY = "key:serverUrl"
    private val USERNAME_KEY = "key:username"
    private val PASSWORD_KEY = "key:password"

    private lateinit var prefs : SharedPreferences

    companion object{

        private var appPreferences: AppPreferences? = null

        fun init(context: Context){
            appPreferences = AppPreferences(context)
        }

        fun getInstance() : AppPreferences?{
            return appPreferences
        }
    }

    constructor(context : Context?){
        if(context != null){
            prefs = context.getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }

    fun putUserAuthentication(username: String, password: String){
        putValue(USERNAME_KEY, username)
        putValue(PASSWORD_KEY, password)
    }

    fun putValue(key: String, value: String){
        var editor = prefs.edit()

        if(prefs.contains(key)){
            editor.remove(key)
        }

        editor.putString(key, value)
        editor.apply()
    }

    fun getValue(key: String) : String{
        return prefs.getString(key, null)
    }

    fun clearData(){
        prefs.edit().clear().apply()
    }

    fun isUserLoggedIn() : Boolean{
        return appPreferences!!.getValue(USERNAME_KEY) != null
                && appPreferences!!.getValue(PASSWORD_KEY) != null
    }
}
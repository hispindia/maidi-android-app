package com.app.maidi.domains.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.login.LoginActivity
import com.app.maidi.utils.AppPreferences

class LoginInputUsernameFragment : BaseFragment(), View.OnClickListener{

    private lateinit var appPreferences: AppPreferences
    private lateinit var edtUsername: EditText

    companion object{
        fun newInstance() : LoginInputUsernameFragment{
            return LoginInputUsernameFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        appPreferences = AppPreferences(activity)

        var groupView = inflater!!.inflate(R.layout.fragment_login_input_username, container, false)
        var btnNext = groupView.findViewById<TextView>(R.id.fragment_login_input_username_btn_next)
        edtUsername = groupView.findViewById<EditText>(R.id.fragment_login_input_username_edt_username)
        btnNext.setOnClickListener(this)
        return groupView
    }

    override fun onClick(p0: View?) {
        if(edtUsername.text.toString().isEmpty()){
            Toast.makeText(activity, "Please enter a username", Toast.LENGTH_LONG).show()
            return
        }

        appPreferences.putUsername(edtUsername.text.toString())
        (activity as LoginActivity)
            .transformFragment(R.id.activity_login_fl_fragment, LoginInputPasswordFragment.newInstance())
    }
}
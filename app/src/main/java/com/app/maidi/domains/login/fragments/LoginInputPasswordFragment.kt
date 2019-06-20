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
import com.app.maidi.utils.Constants

class LoginInputPasswordFragment : BaseFragment(), View.OnClickListener {

    private lateinit var appPreferences: AppPreferences
    private lateinit var edtPassword: EditText

    companion object{
        fun newInstance() : LoginInputPasswordFragment{
            return LoginInputPasswordFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        appPreferences = AppPreferences(activity)

        var groupView: View = inflater.inflate(R.layout.fragment_login_input_password, container, false)
        edtPassword = groupView.findViewById<EditText>(R.id.fragment_login_input_password_edt_password)
        var btnLogin = groupView.findViewById<TextView>(R.id.fragment_login_input_password_btn_login)
        btnLogin.setOnClickListener(this)
        return groupView
    }

    override fun onClick(p0: View?) {
        val password = edtPassword.text.toString()
        (activity as LoginActivity).loginPresenter.login(appPreferences.getUsername(), password)
    }
}
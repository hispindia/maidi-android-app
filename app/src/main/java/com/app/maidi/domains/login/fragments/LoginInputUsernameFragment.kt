package com.app.maidi.domains.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.login.LoginActivity

class LoginInputUsernameFragment : BaseFragment(){



    companion object{
        fun newInstance() : LoginInputUsernameFragment{
            return LoginInputUsernameFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var groupView = inflater!!.inflate(R.layout.fragment_login_input_username, container, false)
        var btnNext = groupView.findViewById<TextView>(R.id.fragment_login_input_username_btn_next)
        btnNext.setOnClickListener { view ->
            (activity as LoginActivity)
                .transformFragment(R.id.activity_login_fl_fragment, LoginInputPasswordFragment.newInstance())
        }
        return groupView
    }

}
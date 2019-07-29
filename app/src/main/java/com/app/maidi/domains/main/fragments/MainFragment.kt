package com.app.maidi.domains.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.child_registration.ChildRegistrationActivity
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.fragments.immunisation.immunisation_card.ImmunisationChooseFragment
import com.app.maidi.utils.Constants

class MainFragment : BaseFragment() {

    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var viewGroup = inflater.inflate(R.layout.fragment_main, container, false)
        ButterKnife.bind(this, viewGroup)
        mainActivity = activity as MainActivity
        return viewGroup
    }

    override fun onResume() {
        super.onResume()
        mainActivity.transparentActionBar()
    }

    @OnClick(R.id.activity_main_fl_child_registration)
    fun onChildRegistrationModuleClicked(){
        var extras = Bundle()
        extras.putBoolean(Constants.GUEST_ROLE, false)
        mainActivity.transformActivity(mainActivity, ChildRegistrationActivity::class.java, false, extras)
    }

    @OnClick(R.id.activity_main_fl_immunization_card)
    fun onMyImmunisationModuleClicked(){
        /*mainActivity.transformFragment(R.id.activity_main_fl_content,
            ImmunisationChooseFragment()
        )*/
    }
}
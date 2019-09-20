package com.app.maidi.domains.main.fragments.child_registration

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
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.utils.Constants

class ChildRegistrationChooseFragment : BaseFragment() {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_child_registration_choose, container, false)
        ButterKnife.bind(this, viewGroup)

        return viewGroup
    }

    @OnClick(R.id.fragment_child_registration_choose_cv_enroll_new_child)
    fun onEnrollButtonClicked(){
        var bundle = Bundle()
        bundle.putString(ChildRegistrationActivity.PROGRAM, Constants.IMMUNISATION)
        mainActivity.transformActivity(mainActivity, ChildRegistrationActivity::class.java, false, bundle)
    }

    @OnClick(R.id.fragment_child_registration_choose_cv_registered_beneficaries)
    fun onShowRegisteredItemsButtonClicked(){
        mainActivity.transformFragment(
            R.id.activity_main_fl_content,
            RegisteredBeneficariesFragment()
        )
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.child_registration))
        mainActivity.isSwipeForceSyncronizeEnabled(false)
    }

    fun createPresenter() : MainPresenter {
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
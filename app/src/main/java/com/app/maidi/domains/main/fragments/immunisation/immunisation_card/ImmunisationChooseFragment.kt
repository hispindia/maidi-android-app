package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.R
import com.app.maidi.domains.base.BaseFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.domains.main.MainPresenter
import com.app.maidi.domains.main.fragments.immunisation.session_wise.SessionWiseChooseDateFragment

class ImmunisationChooseFragment : BaseFragment() {

    lateinit var mainActivity: MainActivity
    lateinit var mainPresenter: MainPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = activity as MainActivity
        createPresenter()

        var viewGroup = inflater.inflate(R.layout.fragment_immunisation_choose, container, false)
        ButterKnife.bind(this, viewGroup)

        return viewGroup
    }

    @OnClick(R.id.fragment_immunisation_choose_fl_immunisation_card)
    fun onImmunisationCardButtonClicked(){
        mainActivity.transformFragment(R.id.activity_main_fl_content,
            ImmunisationCardFragment()
        )
    }

    @OnClick(R.id.fragment_immunisation_choose_fl_session_wise)
    fun onSessionWiseButtonClicked(){
        mainActivity.transformFragment(R.id.activity_main_fl_content,
            SessionWiseChooseDateFragment()
        )
    }

    override fun onResume() {
        super.onResume()
        mainActivity.solidActionBar(resources.getString(R.string.immunisation_card_title))
    }

    fun createPresenter() : MainPresenter{
        mainPresenter = mainActivity.mainPresenter
        return mainPresenter
    }
}
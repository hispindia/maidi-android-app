package com.app.maidi.domains.my_registration.list_my_registration

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.DaggerLoginComponent
import com.app.maidi.infrastructures.ActivityModules
import org.hisp.dhis.android.sdk.persistence.models.Enrollment
import javax.inject.Inject

class ListMyRegistrationActivity : BaseActivity<ListMyRegistrationView, ListMyRegistrationPresenter>(){

    @Inject
    lateinit var listMyRegistrationPresenter: ListMyRegistrationPresenter

    lateinit var mainApplication: MainApplication

    @BindView(R.id.activity_list_my_registration_actionbar)
    lateinit var actionBar: View

    @BindView(R.id.activity_list_my_registration_rcv_list)
    lateinit var rcvList: RecyclerView

    @BindView(R.id.activity_list_my_registration_srl_refresh)
    lateinit var srlRefresh : SwipeRefreshLayout

    lateinit var title: TextView
    lateinit var backButton : ImageView
    lateinit var adapter: ListMyRegistrationAdapter
    lateinit var dividerItemDecoration: DividerItemDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_my_registration)
        ButterKnife.bind(this)

        title = actionBar.findViewById(R.id.layout_actionbar_tv_title)
        title.text = "12/25/2019"

        backButton = actionBar.findViewById(R.id.layout_actionbar_iv_action)
        backButton.setOnClickListener {
            onBackPressed()
        }

        dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.bg_divider))
        adapter = ListMyRegistrationAdapter(this, arrayListOf())
        rcvList.layoutManager = LinearLayoutManager(this)
        rcvList.addItemDecoration(dividerItemDecoration)
        rcvList.adapter = adapter
    }

    override fun createPresenter(): ListMyRegistrationPresenter {
        mainApplication = getApplication() as MainApplication
        DaggerListMyRegistrationComponent.builder()
            .appComponent(mainApplication.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return listMyRegistrationPresenter
    }
}
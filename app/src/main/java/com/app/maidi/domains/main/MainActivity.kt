package com.app.maidi.domains.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.DaggerLoginComponent
import com.app.maidi.domains.login.LoginActivity
import com.app.maidi.infrastructures.ActivityModules
import com.special.ResideMenu.ResideMenu
import com.squareup.otto.Subscribe
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.DhisService
import org.hisp.dhis.android.sdk.controllers.PeriodicSynchronizerController
import org.hisp.dhis.android.sdk.controllers.UserController
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent
import org.hisp.dhis.android.sdk.events.UiEvent
import org.hisp.dhis.android.sdk.persistence.Dhis2Application
import org.hisp.dhis.android.sdk.persistence.preferences.AppPreferences
import org.hisp.dhis.android.sdk.utils.UiUtils
import javax.inject.Inject


class MainActivity : BaseActivity<MainView, MainPresenter>(), View.OnClickListener, MainView{

    @Inject
    lateinit var mainPresenter: MainPresenter

    lateinit var application: MainApplication

    lateinit var resideMenu: ResideMenu

    lateinit var llRestore: LinearLayout
    lateinit var llSignOut: LinearLayout

    lateinit var userRoleId: String

    @BindView(R.id.activity_main_rl_content)
    lateinit var rlContent: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRoleId = AppPreferences(this).userRole

        var roleUsers = MetaDataController.getRoleUsers()

        for(role in roleUsers){
            if(role.id.equals(userRoleId)){
                if(role.name.equals("Guest role"))
                    setContentView(R.layout.activity_main_beneficiary)
                else
                    setContentView(R.layout.activity_main)
            }
        }

        ButterKnife.bind(this)

        PeriodicSynchronizerController.activatePeriodicSynchronizer(this)

        resideMenu = ResideMenu(this, R.layout.layout_main_menu, -1)
        resideMenu.setBackground(R.color.dark_blue)
        resideMenu.attachToActivity(this)
        resideMenu.setScaleValue(0.55f)

        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT)

        llRestore = resideMenu.leftMenuView.findViewById(R.id.layout_main_menu_ll_restore)
        llSignOut = resideMenu.leftMenuView.findViewById(R.id.layout_main_menu_ll_signout)

        llRestore.setOnClickListener(this)
        llSignOut.setOnClickListener(this)

        setupCloseMenu(rlContent, this)
    }

    override fun onResume() {
        super.onResume()
        Dhis2Application.bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        Dhis2Application.bus.unregister(this)
    }

    @OnClick(R.id.activity_main_iv_menu)
    fun onMenuClicked(){
        if(!resideMenu.isOpened) {
            resideMenu.openMenu(ResideMenu.DIRECTION_LEFT)
        }else{
            resideMenu.closeMenu()
        }
    }

    @Subscribe
    fun updateUiEvent(uiEvent: UiEvent){
        if(uiEvent.eventType.equals(UiEvent.UiEventType.SYNCING_END)){
            hideLoading()
            Toast.makeText(this, "Sync completed", Toast.LENGTH_SHORT).show()
        }
    }

    @Subscribe
    fun loadingEvent(loadingMessageEvent: LoadingMessageEvent){
        updateText(loadingMessageEvent.message)
        //Toast.makeText(this, loadingMessageEvent.message , Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
        showHUD()
    }

    override fun hideLoading() {
        hideHUD()
    }

    override fun onClick(view: View?) {
        if(view!!.tag.equals("ll_restore")){
            showLoading()
            DhisService.synchronize(this)
            resideMenu.closeMenu()
        }else if(view!!.tag.equals("ll_signout")){
            logout()
            resideMenu.closeMenu()
        }
    }

    fun setupCloseMenu(view: View, activity: AppCompatActivity) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is FrameLayout) {
            view.setOnTouchListener { v, event ->
                resideMenu.closeMenu()
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupCloseMenu(innerView, activity)
            }
        }
    }

    fun logout(){
        UiUtils.showConfirmDialog(
            this, getString(org.hisp.dhis.android.sdk.R.string.logout_title),
            getString(org.hisp.dhis.android.sdk.R.string.logout_message),
            getString(org.hisp.dhis.android.sdk.R.string.logout), getString(org.hisp.dhis.android.sdk.R.string.cancel)
        ) { dialog, which ->
            if (DhisController.hasUnSynchronizedDatavalues) {
                //show error dialog
                UiUtils.showErrorDialog(
                    this,
                    getString(org.hisp.dhis.android.sdk.R.string.error_message),
                    getString(org.hisp.dhis.android.sdk.R.string.unsynchronized_data_values)
                ) { dialog, which -> dialog.dismiss() }
            } else {
                val session = DhisController.getInstance().session
                if (session != null) {
                    val httpUrl = session.serverUrl
                    if (httpUrl != null) {
                        val serverUrlString = httpUrl.toString()
                        val appPreferences = AppPreferences(
                            this.getApplicationContext()
                        )
                        appPreferences.putServerUrl(serverUrlString)
                    }
                }
                DhisService.logOutUser(this)
                val intent = Intent(this.getApplicationContext(), LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun createPresenter(): MainPresenter {
        application = getApplication() as MainApplication
        DaggerMainComponent.builder()
            .appComponent(application.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return mainPresenter
    }

}

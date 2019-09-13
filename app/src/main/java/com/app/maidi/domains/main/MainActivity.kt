package com.app.maidi.domains.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.BuildConfig
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.LoginActivity
import com.app.maidi.domains.main.fragments.MainBeneficiaryFragment
import com.app.maidi.domains.main.fragments.MainFragment
import com.app.maidi.domains.main.fragments.aefi.RegisteredCasesFragment
import com.app.maidi.domains.main.fragments.immunisation.immunisation_card.ImmunisationCardFragment
import com.app.maidi.domains.main.fragments.immunisation.session_wise.SessionWiseDataListFragment
import com.app.maidi.domains.main.fragments.survey.ListSurveyFragment
import com.app.maidi.domains.main.fragments.workplan.MonthlyWorkplanDetailFragment
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.models.Dose
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.utils.Constants
import com.special.ResideMenu.ResideMenu
import com.squareup.otto.Subscribe
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.DhisService
import org.hisp.dhis.android.sdk.controllers.PeriodicSynchronizerController
import org.hisp.dhis.android.sdk.controllers.SyncStrategy
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent
import org.hisp.dhis.android.sdk.events.UiEvent
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.persistence.Dhis2Application
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import org.hisp.dhis.android.sdk.persistence.preferences.AppPreferences
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryFragment
import org.hisp.dhis.android.sdk.utils.UiUtils
import java.io.File
import javax.inject.Inject


class MainActivity : BaseActivity<MainView, MainPresenter>(), View.OnClickListener, MainView{

    companion object{
        val STORAGE_PERMISSION_REQUEST = 1234
    }

    @Inject
    lateinit var mainPresenter: MainPresenter

    lateinit var application: MainApplication

    lateinit var resideMenu: ResideMenu

    lateinit var llRestore: LinearLayout
    lateinit var llSignOut: LinearLayout
    lateinit var tvAccountName: TextView

    lateinit var userRoleId: String
    lateinit var ivMenu: ImageView
    lateinit var ivCreate: ImageView
    lateinit var ivReferral: ImageView
    lateinit var tvTitle: TextView

    @BindView(R.id.activity_main_srl_force_syncronize)
    lateinit var srlForceSyncronize: SwipeRefreshLayout

    @BindView(R.id.activity_main_rl_content)
    lateinit var rlContent: LinearLayout

    @BindView(R.id.activity_main_actionbar)
    lateinit var actionbar: RelativeLayout

    var isReloadActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        resideMenu = ResideMenu(this, R.layout.layout_main_menu, -1)
        resideMenu.setBackground(R.color.dark_blue)
        resideMenu.attachToActivity(this)
        resideMenu.setScaleValue(0.55f)

        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT)

        llRestore = resideMenu.leftMenuView.findViewById(R.id.layout_main_menu_ll_restore)
        llSignOut = resideMenu.leftMenuView.findViewById(R.id.layout_main_menu_ll_signout)
        tvAccountName = resideMenu.leftMenuView.findViewById(R.id.layout_main_menu_tv_account_name)

        llRestore.setOnClickListener(this)
        llSignOut.setOnClickListener(this)
        var userAccount = MetaDataController.getUserAccount()
        if(userAccount != null && userAccount.displayName != null){
            tvAccountName.text = userAccount.displayName
        }

        ivMenu = actionbar.findViewById(R.id.layout_actionbar_iv_action)
        ivCreate = actionbar.findViewById(R.id.layout_actionbar_iv_create)
        ivReferral = actionbar.findViewById(R.id.layout_actionbar_iv_referral)
        tvTitle = actionbar.findViewById(R.id.layout_actionbar_tv_title)

        setupCloseMenu(rlContent, this)

        srlForceSyncronize.setOnRefreshListener {
            showLoading()
            DhisService.forceSynchronize(this)
        }

        initView()
    }

    fun initView(){
        userRoleId = AppPreferences(this).userRole

        var roleUsers = MetaDataController.getRoleUsers()

        if(roleUsers != null && roleUsers.size > 0) {
            for (role in roleUsers) {
                if (role.id.equals(userRoleId)) {
                    if ((role.name != null && role.name.equals(Constants.GUEST_ROLE))
                            || (role.displayName != null && role.displayName.equals(Constants.GUEST_ROLE)))
                        transformFragment(R.id.activity_main_fl_content, MainBeneficiaryFragment())
                    else
                        transformFragment(R.id.activity_main_fl_content, MainFragment())
                }
            }
            isReloadActivity = false
        }else{
            Toast.makeText(applicationContext, "Missing meta data. Please wait for reloading", Toast.LENGTH_LONG).show()
            isReloadActivity = true
            showLoading()
            DhisService.forceSynchronize(this)
            return
        }
        PeriodicSynchronizerController.activatePeriodicSynchronizer(this)
    }

    fun isSwipeForceSyncronizeEnabled(isEnabled : Boolean){
        if(srlForceSyncronize != null){
            srlForceSyncronize.isEnabled = isEnabled
        }
    }

    @Subscribe
    fun updateUiEvent(uiEvent: UiEvent){
        when(uiEvent.eventType){
            UiEvent.UiEventType.SYNCING_END -> {
                hideLoading()
                Toast.makeText(this, "Sync completed", Toast.LENGTH_SHORT).show()
                if(isReloadActivity)
                    initView()
                if(srlForceSyncronize != null){
                    srlForceSyncronize.isRefreshing = false
                }
            }
            UiEvent.UiEventType.START_SEND_DATA -> showLoading()
            UiEvent.UiEventType.ERROR_SEND_DATA -> hideLoading()
            UiEvent.UiEventType.SUCCESS_SEND_DATA -> {
                hideLoading()
                Toast.makeText(
                    this,
                    resources.getString(R.string.create_update_successful),
                    Toast.LENGTH_LONG
                ).show()
                onBackPressed()
            }
        }
    }

    @Subscribe
    fun loadingEvent(loadingMessageEvent: LoadingMessageEvent){
        updateText(loadingMessageEvent.message)
        //Toast.makeText(this, loadingMessageEvent.message , Toast.LENGTH_SHORT).show()
    }

    fun transparentActionBar(){
        actionbar.setBackgroundResource(android.R.color.transparent)
        tvTitle.text = ""
        ivMenu.setImageResource(R.drawable.ic_black_menu)
        ivMenu.imageTintList = null

        ivMenu.setOnClickListener({
            if(!resideMenu.isOpened) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT)
            }else{
                resideMenu.closeMenu()
            }
        })
    }

    fun solidActionBar(title: String){
        actionbar.setBackgroundResource(R.color.dark_blue)
        tvTitle.text = title
        ivMenu.setImageResource(R.drawable.ic_arrow_left)
        ivMenu.imageTintList = ColorStateList.valueOf(Color.WHITE)

        ivMenu.setOnClickListener({
            onBackPressed()
        })

        ivCreate.visibility = View.GONE
        ivReferral.visibility = View.GONE
        ivCreate.setOnClickListener(null)
        ivReferral.setOnClickListener(null)
    }

    fun solidActionBar(title: String, createButtonlistener: View.OnClickListener){
        actionbar.setBackgroundResource(R.color.dark_blue)
        tvTitle.text = title
        ivMenu.setImageResource(R.drawable.ic_arrow_left)
        ivMenu.imageTintList = ColorStateList.valueOf(Color.WHITE)

        ivMenu.setOnClickListener({
            onBackPressed()
        })

        ivCreate.visibility = View.VISIBLE
        ivCreate.setOnClickListener(createButtonlistener)
        ivReferral.visibility = View.GONE
        ivReferral.setOnClickListener(null)
    }

    fun solidActionBar(title: String, createButtonIcon: Int, createButtonlistener: View.OnClickListener){
        actionbar.setBackgroundResource(R.color.dark_blue)
        tvTitle.text = title
        ivMenu.setImageResource(R.drawable.ic_arrow_left)
        ivMenu.imageTintList = ColorStateList.valueOf(Color.WHITE)

        ivMenu.setOnClickListener({
            onBackPressed()
        })

        ivCreate.visibility = View.VISIBLE
        ivCreate.setImageResource(createButtonIcon)
        ivCreate.setOnClickListener(createButtonlistener)
        ivReferral.visibility = View.GONE
        ivReferral.setOnClickListener(null)
    }

    fun solidActionBar(title: String, createButtonlistener: View.OnClickListener, referralButtonListener: View.OnClickListener){
        actionbar.setBackgroundResource(R.color.dark_blue)
        tvTitle.text = title
        ivMenu.setImageResource(R.drawable.ic_arrow_left)
        ivMenu.imageTintList = ColorStateList.valueOf(Color.WHITE)

        ivMenu.setOnClickListener({
            onBackPressed()
        })

        ivCreate.visibility = View.VISIBLE
        ivCreate.setOnClickListener(createButtonlistener)

        ivReferral.visibility = View.VISIBLE
        ivReferral.setOnClickListener(referralButtonListener)
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

    override fun onResume() {
        super.onResume()
        Dhis2Application.bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        Dhis2Application.bus.unregister(this)
    }

    override fun showLoading() {
        runOnUiThread {
            showHUD()
        }
    }

    override fun hideLoading() {
        runOnUiThread {
            hideHUD()
        }
    }

    override fun onClick(view: View?) {
        if(view!!.tag.equals("ll_restore")){
            showLoading()
            DhisService.forceSynchronize(this)
            resideMenu.closeMenu()
        }else if(view!!.tag.equals("ll_signout")){
            logout()
            resideMenu.closeMenu()
        }
    }

    override fun createPresenter(): MainPresenter {
        application = getApplication() as MainApplication
        application.getMainComponent()!!.inject(this)
        return mainPresenter
    }

    // ******************* MainView functions *****************

    override fun getAefiTrackedEntityInstances(trackedEntityInstances: List<TrackedEntityInstance>) {
        runOnUiThread {
            if(isCurrentFragment<RegisteredCasesFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<RegisteredCasesFragment>(R.id.activity_main_fl_content).getRemoteTrackedEntityInstances(trackedEntityInstances)
            }
            hideHUD()
        }
    }

    override fun getImmunisationCardListSuccess(immunisationList: List<ImmunisationCard>) {
        runOnUiThread {
            if(isCurrentFragment<ImmunisationCardFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<ImmunisationCardFragment>(R.id.activity_main_fl_content).updateImmunisationCardList(immunisationList)
            }
            hideHUD()
        }

    }

    override fun getSessionWiseDataListSuccess(sessionWiseList: List<ImmunisationCard>) {
        runOnUiThread {
            if(isCurrentFragment<SessionWiseDataListFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<SessionWiseDataListFragment>(R.id.activity_main_fl_content).getSessionWiseDataList(sessionWiseList)
            }
        }
    }

    override fun getProgramDataElements(dataElements: List<DataElement>) {
        runOnUiThread {
            if(isCurrentFragment<SessionWiseDataListFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<SessionWiseDataListFragment>(R.id.activity_main_fl_content).getProgramDataElements(dataElements)
            }
        }
    }

    override fun getTotalDoseList(doseList: List<Dose>) {
        runOnUiThread {
            if(isCurrentFragment<SessionWiseDataListFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<SessionWiseDataListFragment>(R.id.activity_main_fl_content).getTotalDoses(doseList)
            }
        }
    }

    override fun getSurveyEvents(events: List<Event>) {
        runOnUiThread {
            if(isCurrentFragment<ListSurveyFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<ListSurveyFragment>(R.id.activity_main_fl_content).getEventsListSuccess(events)
            }
            hideHUD()
        }
    }

    override fun getWorkplanEvents(events: List<Event>) {
        runOnUiThread {
            if(isCurrentFragment<MonthlyWorkplanDetailFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<MonthlyWorkplanDetailFragment>(R.id.activity_main_fl_content).getWorkplanList(events)
            }
            hideHUD()
        }
    }

    override fun getApiFailed(exception: APIException) {
        runOnUiThread {
            Log.e(MainActivity::class.simpleName, exception.toString())
            hideHUD()
        }
    }

    override fun onBackPressed() {
        if(isCurrentFragment<EventDataEntryFragment>(R.id.activity_main_fl_content)){
            var handled = getCurrentFragment<EventDataEntryFragment>(R.id.activity_main_fl_content).onBackPressed()
            if(handled)
                super.onBackPressed()
        }else {
            super.onBackPressed()
        }
    }

    // *******************THE END - MainView functions *****************

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig!!.orientation == Configuration.ORIENTATION_PORTRAIT) {
            rlContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        } else if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rlContent.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportSurveyToPdf()
                    exportImmunisationListToPdf()
                } else {
                    showFailedPermissionToast()
                }
                return
            }
        }
    }

    fun checkStoragePermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showFailedPermissionToast()
            } else {*/
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST)
            //}
        } else {
            exportSurveyToPdf()
            exportImmunisationListToPdf()
        }
    }

    fun showFailedPermissionToast(){
        Toast.makeText(this, resources.getString(R.string.write_storage_permission), Toast.LENGTH_LONG).show()
        openAppPermissionSettingScreen()
        finish()
    }

    fun openAppPermissionSettingScreen(){
        var intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", getPackageName(), null)
        intent.setData(uri)
        startActivity(intent)
    }

    fun exportSurveyToPdf(){
        runOnUiThread {
            if(isCurrentFragment<ListSurveyFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<ListSurveyFragment>(R.id.activity_main_fl_content).exportSurveyToPdf()
            }
            hideHUD()
        }
    }

    fun exportImmunisationListToPdf(){
        runOnUiThread {
            if(isCurrentFragment<ImmunisationCardFragment>(R.id.activity_main_fl_content)){
                getCurrentFragment<ImmunisationCardFragment>(R.id.activity_main_fl_content).exportDatasToPdf()
            }
            hideHUD()
        }
    }

    fun openExportFolderDialog(file: File){
        AlertDialog.Builder(this)
            .setTitle("Export files successful")
            .setMessage(resources.getString(R.string.export_pdf_file_success))
            .setPositiveButton("Open file", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    var intent = Intent(Intent.ACTION_VIEW)
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    var uri = FileProvider.getUriForFile(this@MainActivity, BuildConfig.APPLICATION_ID, file)
                    intent.setDataAndType(uri, "application/pdf");
                    var pm = getPackageManager()
                    if (intent.resolveActivity(pm) != null) {
                        startActivity(intent);
                    }
                }
            })
            .create()
            .show()
    }
}

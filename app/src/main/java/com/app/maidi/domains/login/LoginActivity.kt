package com.app.maidi.domains.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.app.maidi.MainApplication
import com.app.maidi.R
import com.app.maidi.utils.MaidiCrashManagerListener
import com.app.maidi.domains.base.BaseActivity
import com.app.maidi.domains.login.fragments.LoginMainFragment
import com.app.maidi.domains.main.MainActivity
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.models.database.User
import com.app.maidi.utils.MethodUtils.Companion.setupEditTextKeyboard
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.gson.Gson
import com.squareup.otto.Subscribe
import net.hockeyapp.android.CrashManager
import org.hisp.dhis.android.sdk.controllers.DhisController
import org.hisp.dhis.android.sdk.controllers.DhisService
import org.hisp.dhis.android.sdk.controllers.LoadingController
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent
import org.hisp.dhis.android.sdk.events.UiEvent
import org.hisp.dhis.android.sdk.job.NetworkJob
import org.hisp.dhis.android.sdk.network.APIException
import org.hisp.dhis.android.sdk.network.Credentials
import org.hisp.dhis.android.sdk.persistence.Dhis2Application
import org.hisp.dhis.android.sdk.persistence.preferences.AppPreferences
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType
import org.hisp.dhis.android.sdk.services.StartPeriodicSynchronizerService
import org.hisp.dhis.android.sdk.utils.Utils
import javax.inject.Inject

class LoginActivity : BaseActivity<LoginView, LoginPresenter>(), LoginView{

    @Inject
    lateinit var loginPresenter: LoginPresenter

    @BindView(R.id.activity_login_ll_content)
    lateinit var llContent: LinearLayout

    lateinit var application: MainApplication
    lateinit var appPreferences: AppPreferences

    private var isPulling: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        setupEditTextKeyboard(llContent, this)

        FirebaseAuth.getInstance().signOut()
        FirebaseApp.initializeApp(this)
        appPreferences = AppPreferences(applicationContext)

        DhisController.getInstance().init()
        if(DhisController.isUserLoggedIn()){
            transformActivity(this, MainActivity::class.java, true)
            startService(Intent(this, StartPeriodicSynchronizerService::class.java))
            return
        }

        transformFragment(R.id.activity_login_fl_fragment, LoginMainFragment())
    }

    override fun createPresenter(): LoginPresenter {
        application = getApplication() as MainApplication
        DaggerLoginComponent.builder()
            .appComponent(application.getApplicationComponent())
            .activityModules(ActivityModules(this))
            .build()
            .inject(this)
        return loginPresenter
    }

    @Subscribe
    fun updateUiEvent(uiEvent: UiEvent){
        if(uiEvent.eventType.equals(UiEvent.UiEventType.SYNCING_END)){
            isPulling = false
            hideLoading()
            Toast.makeText(this, "Sync completed", Toast.LENGTH_SHORT).show()
            clearStack()
            transformActivity(this, MainActivity::class.java, true)
            startService(Intent(this, StartPeriodicSynchronizerService::class.java))
        }
    }

    @Subscribe
    fun loadingEvent(loadingMessageEvent: LoadingMessageEvent){
        updateText(loadingMessageEvent.message)
    }

    @Subscribe
    fun onLoginFinished(result: NetworkJob.NetworkJobResult<ResourceType>){
        if (result != null && ResourceType.USERS == result.getResourceType()) {
            if (result.getResponseHolder().getApiException() == null) {

                LoadingController.enableLoading(this, ResourceType.ASSIGNEDPROGRAMS)
                LoadingController.enableLoading(this, ResourceType.OPTIONSETS)
                LoadingController.enableLoading(this, ResourceType.PROGRAMS)
                LoadingController.enableLoading(this, ResourceType.CONSTANTS)
                LoadingController.enableLoading(this, ResourceType.PROGRAMRULES)
                LoadingController.enableLoading(this, ResourceType.PROGRAMRULEVARIABLES)
                LoadingController.enableLoading(this, ResourceType.PROGRAMRULEACTIONS)
                LoadingController.enableLoading(this, ResourceType.RELATIONSHIPTYPES)
                LoadingController.enableLoading(this, ResourceType.EVENTS)
                LoadingController.enableLoading(this, ResourceType.USERROLES)
                LoadingController.enableLoading(this, ResourceType.ORGANISATIONUNIT)
                LoadingController.enableLoading(this, ResourceType.TRACKEDENTITYINSTANCE)
                isPulling = true
                DhisService.loadInitialData(this)
            } else {
                onLoginFail(result.getResponseHolder().getApiException())
            }
        }
    }

    fun onLoginFail(exception: APIException){
        if(exception.response == null){
            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
        }else{
            if(exception.response.code() == 401)
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Unable to log in", Toast.LENGTH_SHORT).show()
        }
        hideLoading()
    }

    override fun onPause() {
        super.onPause()
        Dhis2Application.bus.unregister(this)
    }

    override fun onResume() {
        super.onResume()
        Dhis2Application.bus.register(this)
        if(isPulling){
            DhisService.loadInitialData(this)
        }
        CrashManager.register(this, MaidiCrashManagerListener())
    }

    override fun showLoading() {
        showHUD()
    }

    override fun hideLoading() {
        hideHUD()
    }

    override fun signInWithVerifyCodeSuccess(phoneNumber : String) {
        var jsonInfoString = Utils.getDataFromAssetFile(assets, "info.json")
        var credentials = Gson().fromJson<Credentials>(jsonInfoString, Credentials::class.java)
        var decryptPassword = Utils.decryptStrAndFromBase64(credentials.password)
        loginPresenter.login(credentials.username, decryptPassword, phoneNumber, true)
    }

    override fun getAccountInfo(user: User) {
        Toast.makeText(this, user.displayName, Toast.LENGTH_SHORT).show()
    }

    override fun getApiFailed(exception: Exception?) {
        hideLoading()
        if(exception is FirebaseAuthInvalidCredentialsException){
            Dhis2Application.bus.post(exception as FirebaseAuthInvalidCredentialsException)
            Log.e("FirebaseException", exception.message)
        }else{
            Toast.makeText(this, exception!!.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun clearStack(){
        val backStackEntry = supportFragmentManager.backStackEntryCount
        if (backStackEntry > 0) {
            for (i in 0 until backStackEntry) {
                supportFragmentManager.popBackStackImmediate()
            }
        }

        //Here we are removing all the fragment that are shown here
        if (supportFragmentManager.fragments != null && supportFragmentManager.fragments.size > 0) {
            for (i in 0 until supportFragmentManager.fragments.size) {
                val mFragment = supportFragmentManager.fragments[i]
                if (mFragment != null) {
                    supportFragmentManager.beginTransaction().remove(mFragment).commit()
                }
            }
        }}
}
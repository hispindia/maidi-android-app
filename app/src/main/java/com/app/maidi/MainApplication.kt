package com.app.maidi

import android.app.Activity
import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.amitshekhar.DebugDB
import com.app.maidi.database.MaidiDatabase
import com.app.maidi.domains.main.DaggerMainComponent
import com.app.maidi.domains.main.MainComponent
import com.app.maidi.infrastructures.ActivityModules
import com.app.maidi.infrastructures.AppComponent
import com.app.maidi.infrastructures.AppModules
import com.app.maidi.infrastructures.DaggerAppComponent
import com.app.maidi.utils.AppPreferences
import com.google.firebase.FirebaseApp
import com.raizlabs.android.dbflow.config.FlowManager
import okhttp3.internal.Util
import org.hisp.dhis.android.sdk.persistence.Dhis2Application
import org.hisp.dhis.android.sdk.persistence.Dhis2Database
import org.hisp.dhis.android.sdk.persistence.migrations.MigrationUtil
import org.hisp.dhis.android.sdk.utils.Utils
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import javax.inject.Inject

class MainApplication : Dhis2Application() {

    private var applicationComponent: AppComponent? = null
    private var mainComponent: MainComponent? = null

    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)

        if(BuildConfig.DEBUG) {
            DebugDB.getAddressLog()
        }
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
            .setDefaultFontPath("fonts/NunitoSans-Regular.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
        )

        setApplicationComponent(
            DaggerAppComponent
            .builder()
            .appModules(AppModules(this))
            .build()
        )

        setMainComponent(
            DaggerMainComponent.builder()
                .appComponent(getApplicationComponent())
                .build()
        )

        var encrypt = Utils.encryptStrAndToBase64("Maidi@123")
        Log.d("Encrypt String", encrypt)
        var decrypt = Utils.decryptStrAndFromBase64(encrypt)
        Log.d("Decrypt String", decrypt)
    }

    fun getApplicationComponent() = applicationComponent

    fun setApplicationComponent(applicationComponent: AppComponent){
        this.applicationComponent = applicationComponent
    }

    fun setMainComponent(mainComponent: MainComponent){
        this.mainComponent = mainComponent
    }

    fun getMainComponent() = mainComponent

    override fun getMainActivity(): Class<out Activity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTerminate() {
        super.onTerminate()
        FlowManager.destroy()
    }
}
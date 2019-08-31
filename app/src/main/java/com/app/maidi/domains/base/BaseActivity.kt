package com.app.maidi.domains.base

import android.app.ActivityOptions
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.maidi.R
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.whiteelephant.monthpicker.MonthPickerDialog
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.lang.Exception
import java.util.*

abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>> : MvpActivity<V, P>(){

    private var progressDialogLoading: Dialog? = null
    private var tvMessage: TextView? = null

    fun transformFragment(container: Int, V : Fragment){
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(container, V)
            .addToBackStack(null)
            .commit()
    }

    fun <T>transformActivity(activity: AppCompatActivity, nextActivity: Class<T>, isFinish: Boolean){
        var intent = Intent(activity, nextActivity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())

        if(isFinish)
            activity.finish()
    }

    fun <T>transformActivity(activity: AppCompatActivity, nextActivity: Class<T>, isFinish: Boolean, bundle : Bundle){
        var intent = Intent(activity, nextActivity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtras(bundle)

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())

        if(isFinish)
            activity.finish()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun showHUD() {
        if (progressDialogLoading != null && progressDialogLoading!!.isShowing()) {
        } else {
            val view = layoutInflater.inflate(R.layout.layout_progress_loading_ball_spin, null)
            tvMessage = view.findViewById<TextView>(R.id.layout_loading_tv_message)
            progressDialogLoading = Dialog(this)
            progressDialogLoading!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialogLoading!!.setContentView(view)
            progressDialogLoading!!.setCancelable(false)
            progressDialogLoading!!.setCanceledOnTouchOutside(false)

            val window = progressDialogLoading!!.getWindow()
            if (window != null) {
                window!!.setBackgroundDrawableResource(R.drawable.bg_layout_loading)
            }
            progressDialogLoading!!.show()
        }
    }

    fun hideHUD() {
        if (progressDialogLoading != null && progressDialogLoading!!.isShowing()) {
            progressDialogLoading!!.dismiss()
        }
    }

    fun updateText(text: String){
        try {
            tvMessage!!.text = text
        }catch (exception : Exception){
            Log.d("Null Exception", exception.toString())
        }
    }

    fun showSelectMonthChooseDialog(datePickerListener: MonthPickerDialog.OnDateSetListener, dueDateCalendar: Calendar){
        var builder = MonthPickerDialog.Builder(this, datePickerListener,
            dueDateCalendar.get(Calendar.YEAR), dueDateCalendar.get(Calendar.MONTH))
        builder.setMinYear(1900)
        builder.setMaxYear(2100)
        builder.setTitle("Select month")
        builder.build().show()
    }

    inline fun <reified T> isCurrentFragment(containerId: Int) : Boolean {
        var fragment = supportFragmentManager.findFragmentById(containerId)
        return fragment is T
    }

    inline fun <reified T> getCurrentFragment(containerId: Int) : T{
        var fragment = supportFragmentManager.findFragmentById(containerId)
        return fragment as T
    }
}
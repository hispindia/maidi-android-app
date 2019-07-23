package com.app.maidi.domains.base

import android.app.Activity
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.app.maidi.R
import com.hannesdorfmann.mosby3.mvp.MvpActivity
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.lang.Exception

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

    fun hideKeyBoard(activity: Activity) {
        try {
            val inputMethodManager = activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!
                    .windowToken, 0
            )
            onWindowFocusChanged(true)
        } catch (e: Exception) {
            Log.d("HideKeyboard", e.toString())
        }

    }

    fun setupEditTextKeyboard(view: View, activity: AppCompatActivity) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideKeyBoard(activity)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupEditTextKeyboard(innerView, activity)
            }
        }
    }
}
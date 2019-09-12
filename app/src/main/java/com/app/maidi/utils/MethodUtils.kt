package com.app.maidi.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.app.maidi.models.Vaccine
import org.joda.time.*

class MethodUtils {

    companion object{

        fun showHideContainer(containerView: View, duration: Int){
            val expand = containerView.visibility != View.VISIBLE
            val prevHeight = containerView.height
            var height = 0
            if (expand) {
                val measureSpecParams = View.MeasureSpec.getSize(View.MeasureSpec.UNSPECIFIED)
                containerView.measure(measureSpecParams, measureSpecParams)
                height = containerView.measuredHeight
            }

            val valueAnimator = ValueAnimator.ofInt(prevHeight, height)
            valueAnimator.addUpdateListener { animation ->
                containerView.layoutParams.height = animation.animatedValue as Int
                containerView.requestLayout()
            }

            valueAnimator.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    if (!expand) {
                        containerView.visibility = View.INVISIBLE
                    }
                }

                override fun onAnimationCancel(p0: Animator?) {

                }

                override fun onAnimationStart(p0: Animator?) {
                    if (expand) {
                        containerView.visibility = View.VISIBLE
                    }
                }
            })
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration.toLong()
            valueAnimator.start()
        }

        fun hideKeyBoard(activity: Activity) {
            try {
                val inputMethodManager = activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!
                        .windowToken, 0
                )
                activity.onWindowFocusChanged(true)
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

        fun createScheduleVaccineList(
            dateOfBirth: LocalDate,
            checkDate: LocalDate,
            injectedVaccineList: ArrayList<Vaccine>,
            totalVaccineList: ArrayList<Vaccine>) : ArrayList<Vaccine>{

            var scheduleVaccineList = arrayListOf<Vaccine>()
            scheduleVaccineList.addAll(injectedVaccineList)
            for(vaccine in totalVaccineList){
                var isHasInjected = false
                for(injectedVaccine in injectedVaccineList){
                    if(vaccine.dataElement.uid.equals(injectedVaccine.dataElement.uid)){
                        isHasInjected = true
                        break
                    }
                }

                if(!isHasInjected){
                    var isNeedToCheckReachDueDate = true
                    if(checkHasPreviousVaccine(vaccine.dataElement.displayName)){
                        if(!checkPreviousVaccineHasInjected(vaccine.dataElement.displayName, injectedVaccineList)){
                            isNeedToCheckReachDueDate = false
                        }
                    }

                    if(isNeedToCheckReachDueDate){
                        if(checkVaccineReachDueDate(vaccine.dataElement.displayName, dateOfBirth, checkDate)){
                            var dueDate = getDueDateForVaccine(vaccine.dataElement.displayName, dateOfBirth)
                            var serverDueDate = DateUtils.convertCalendarToServerString(dueDate.toDate())
                            vaccine.dueDate = serverDueDate
                            scheduleVaccineList.add(vaccine)
                        }
                    }
                }
            }

            return scheduleVaccineList
        }

        fun checkHasPreviousVaccine(vaccineName: String) : Boolean{
            if(
                vaccineName.contains("OPV 1") ||
                vaccineName.contains("OPV 2") ||
                vaccineName.contains("OPV 3") ||
                vaccineName.contains("Pentavalent 1") ||
                vaccineName.contains("Pentavalent 2") ||
                vaccineName.contains("Pentavalent 3") ||
                vaccineName.contains("RV 2") ||
                vaccineName.contains("RV 3") ||
                vaccineName.contains("IPV 2") ||
                vaccineName.contains("MR 2") ||
                vaccineName.contains("JE 2") ||
                vaccineName.contains("OPV Booster") ||
                vaccineName.contains("DPT Booster 1") ||
                vaccineName.contains("DPT Booster 2") ||
                vaccineName.contains("TT 2") ||
                vaccineName.contains("PCV 2") ||
                vaccineName.contains("PCV Booster") ||
                vaccineName.contains("Vitamin A (2nd dose)") ||
                vaccineName.contains("Vitamin A (3rd dose)") ||
                vaccineName.contains("Vitamin A (4th dose)") ||
                vaccineName.contains("Vitamin A (5th dose)") ||
                vaccineName.contains("Vitamin A (6th dose)") ||
                vaccineName.contains("Vitamin A (7th dose)") ||
                vaccineName.contains("Vitamin A (8th dose)") ||
                vaccineName.contains("Vitamin A (9th dose)")
            )
                return true
            return false
        }

        fun checkPreviousVaccineHasInjected(vaccineName: String, injectedVaccineList: ArrayList<Vaccine>) : Boolean{
            var isPreviousVaccineHasInjected = false

            for(injectedVaccine in injectedVaccineList){
                if(
                    (vaccineName.contains("OPV 1")
                        && injectedVaccine.dataElement.displayName.contains("OPV 0")) ||
                    (vaccineName.contains("OPV 2")
                            && injectedVaccine.dataElement.displayName.contains("OPV 1")) ||
                    (vaccineName.contains("OPV 3")
                            && injectedVaccine.dataElement.displayName.contains("OPV 2")) ||
                    (vaccineName.contains("Pentavalent 1")
                            && injectedVaccine.dataElement.displayName.contains("Hep B")) ||
                    (vaccineName.contains("Pentavalent 2")
                            && injectedVaccine.dataElement.displayName.contains("Pentavalent 1")) ||
                    (vaccineName.contains("Pentavalent 3")
                            && injectedVaccine.dataElement.displayName.contains("Pentavalent 2")) ||
                    (vaccineName.contains("RV 2")
                            && injectedVaccine.dataElement.displayName.contains("RV 1")) ||
                    (vaccineName.contains("RV 3")
                            && injectedVaccine.dataElement.displayName.contains("RV 2")) ||
                    (vaccineName.contains("IPV 2")
                            && injectedVaccine.dataElement.displayName.contains("IPV 1")) ||
                    (vaccineName.contains("MR 2")
                            && injectedVaccine.dataElement.displayName.contains("MR 1")) ||
                    (vaccineName.contains("JE 2")
                            && injectedVaccine.dataElement.displayName.contains("JE 1")) ||
                    (vaccineName.contains("OPV Booster")
                            && injectedVaccine.dataElement.displayName.contains("OPV 3")) ||
                    (vaccineName.contains("DPT Booster 1")
                            && injectedVaccine.dataElement.displayName.contains("Pentavalent 3")) ||
                    (vaccineName.contains("DPT Booster 2")
                            && injectedVaccine.dataElement.displayName.contains("DPT Booster 1")) ||
                    (vaccineName.contains("TT 2")
                            && injectedVaccine.dataElement.displayName.contains("TT 1")) ||
                    (vaccineName.contains("PCV 2")
                            && injectedVaccine.dataElement.displayName.contains("PCV 1")) ||
                    (vaccineName.contains("PCV Booster")
                            && injectedVaccine.dataElement.displayName.contains("PCV 2")) ||
                    (vaccineName.contains("Vitamin A (2nd dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (1st dose)")) ||
                    (vaccineName.contains("Vitamin A (3rd dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (2nd dose)")) ||
                    (vaccineName.contains("Vitamin A (4th dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (3rd dose)")) ||
                    (vaccineName.contains("Vitamin A (5th dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (4th dose)")) ||
                    (vaccineName.contains("Vitamin A (6th dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (5th dose)")) ||
                    (vaccineName.contains("Vitamin A (7th dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (6th dose)")) ||
                    (vaccineName.contains("Vitamin A (8th dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (7th dose)")) ||
                    (vaccineName.contains("Vitamin A (9th dose)")
                            && injectedVaccine.dataElement.displayName.contains("Vitamin A (8th dose)"))
                        ){
                    isPreviousVaccineHasInjected = true
                    break
                }
            }

            return isPreviousVaccineHasInjected
        }

        fun checkVaccineReachDueDate(vaccineName: String, dateOfBirth: LocalDate, checkDate: LocalDate): Boolean{
            var isChecked = false
            var days = Days.daysBetween(dateOfBirth, checkDate).days
            var weeks = Weeks.weeksBetween(dateOfBirth, checkDate).weeks
            var months = Months.monthsBetween(dateOfBirth, checkDate).months
            var years = Years.yearsBetween(dateOfBirth, checkDate).years
            if(checkDate.isAfter(dateOfBirth) || checkDate.isEqual(dateOfBirth)){
                if(vaccineName.contains("BCG")
                    || vaccineName.contains("Hep B Birth dose")
                    || (vaccineName.contains("OPV 0") && days <= 15))
                    isChecked = true
            }

            if(weeks >= 6){
                if(vaccineName.contains("OPV 1")
                    || vaccineName.contains("Pentavalent 1")
                    || vaccineName.contains("RV 1")
                    || vaccineName.contains("IPV 1")
                    || vaccineName.contains("PCV 1"))
                    isChecked = true
            }

            if(weeks >= 10){
                if(vaccineName.contains("OPV 2")
                    || vaccineName.contains("Pentavalent 2")
                    || vaccineName.contains("RV 2"))
                    isChecked = true
            }

            if(weeks >= 14){
                if(vaccineName.contains("OPV 3")
                    || vaccineName.contains("Pentavalent 3")
                    || vaccineName.contains("IPV 2")
                    || vaccineName.contains("RV 3")
                    || vaccineName.contains("PCV 2"))
                    isChecked = true
            }

            if(months >= 9){
                if(vaccineName.contains("MR 1")
                    || vaccineName.contains("JE 1")
                    || vaccineName.contains("Vitamin A (1st dose)")
                    || vaccineName.contains("PCV Booster"))
                    isChecked = true
            }

            if(months >= 16){
                if(vaccineName.contains("MR 2")
                    || vaccineName.contains("OPV Booster")
                    || vaccineName.contains("DPT Booster 1")
                    || vaccineName.contains("JE 2")
                    || vaccineName.contains("Vitamin A (2nd dose)"))
                    isChecked = true
            }

            if(years >= 2){
                if(vaccineName.contains("Vitamin A (3rd dose)"))
                    isChecked = true
            }

            if(months >= 30){
                if(vaccineName.contains("Vitamin A (4th dose)"))
                    isChecked = true
            }

            if(years >= 3){
                if(vaccineName.contains("Vitamin A (5th dose)"))
                    isChecked = true
            }

            if(months >= 42){
                if(vaccineName.contains("Vitamin A (6th dose)"))
                    isChecked = true
            }

            if(years >= 4){
                if(vaccineName.contains("Vitamin A (7th dose)"))
                    isChecked = true
            }

            if(months >= 54){
                if(vaccineName.contains("Vitamin A (8th dose)"))
                    isChecked = true
            }

            if(years >= 5){
                if(vaccineName.contains("Vitamin A (9th dose)")
                    || vaccineName.contains("DPT Booster 2"))
                    isChecked = true
            }

            if(years >= 10){
                if(vaccineName.contains("TT 1"))
                    isChecked = true
            }

            if(years >= 16){
                if(vaccineName.contains("TT 2"))
                    isChecked = true
            }

            return isChecked
        }

        fun getDueDateForVaccine(vaccineName: String, dateOfBirth: LocalDate) : LocalDate{
            var dueDate = dateOfBirth

            /*if(vaccineName.contains("BCG")
                || vaccineName.contains("OPV 0")
                || vaccineName.contains("Hep B Birth dose"))*/

            //if(weeks >= 6){
                if(vaccineName.contains("OPV 1")
                    || vaccineName.contains("Pentavalent 1")
                    || vaccineName.contains("RV 1")
                    || vaccineName.contains("IPV 1")
                    || vaccineName.contains("PCV 1"))
                        dueDate = dateOfBirth.plusWeeks(6)
            //}

            //if(weeks >= 10){
                if(vaccineName.contains("OPV 2")
                    || vaccineName.contains("Pentavalent 2")
                    || vaccineName.contains("RV 2"))
                        dueDate = dateOfBirth.plusWeeks(10)
            //}

            //if(weeks >= 14){
                if(vaccineName.contains("OPV 3")
                    || vaccineName.contains("Pentavalent 3")
                    || vaccineName.contains("IPV 2")
                    || vaccineName.contains("RV 3")
                    || vaccineName.contains("PCV 2"))
                        dueDate = dateOfBirth.plusWeeks(14)
            //}

            //if(months >= 9){
                if(vaccineName.contains("MR 1")
                    || vaccineName.contains("JE 1")
                    || vaccineName.contains("Vitamin A (1st dose)")
                    || vaccineName.contains("PCV Booster"))
                        dueDate = dateOfBirth.plusMonths(9)
            //}

            //if(months >= 16){
                if(vaccineName.contains("MR 2")
                    || vaccineName.contains("OPV Booster")
                    || vaccineName.contains("DPT Booster 1")
                    || vaccineName.contains("JE 2")
                    || vaccineName.contains("Vitamin A (2nd dose)"))
                        dueDate = dateOfBirth.plusMonths(16)
            //}

            //if(years >= 2){
                if(vaccineName.contains("Vitamin A (3rd dose)"))
                    dueDate = dateOfBirth.plusYears(2)
            //}

            //if(months >= 30){
                if(vaccineName.contains("Vitamin A (4th dose)"))
                    dueDate = dateOfBirth.plusMonths(30)
            //}

            //if(years >= 3){
                if(vaccineName.contains("Vitamin A (5th dose)"))
                    dueDate = dateOfBirth.plusYears(3)
            //}

            //if(months >= 42){
                if(vaccineName.contains("Vitamin A (6th dose)"))
                    dueDate = dateOfBirth.plusMonths(42)
            //}

            //if(years >= 4){
                if(vaccineName.contains("Vitamin A (7th dose)"))
                    dueDate = dateOfBirth.plusYears(4)
            //}

            //if(months >= 54){
                if(vaccineName.contains("Vitamin A (8th dose)"))
                    dueDate = dateOfBirth.plusMonths(54)
            //}

            //if(years >= 5){
                if(vaccineName.contains("Vitamin A (9th dose)")
                    || vaccineName.contains("DPT Booster 2"))
                    dueDate = dateOfBirth.plusYears(5)
            //}

            //if(years >= 10){
                if(vaccineName.contains("TT 1"))
                    dueDate = dateOfBirth.plusYears(10)
            //}

            //if(years >= 16){
                if(vaccineName.contains("TT 2"))
                    dueDate = dateOfBirth.plusYears(16)
            //}

            return dueDate
        }
    }
}
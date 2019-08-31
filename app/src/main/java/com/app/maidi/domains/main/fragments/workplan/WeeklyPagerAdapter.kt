package com.app.maidi.domains.main.fragments.workplan

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.joda.time.LocalDate

class WeeklyPagerAdapter : FragmentStatePagerAdapter {

    private var weekList: HashMap<String, ArrayList<LocalDate>>
    private var workplanList: List<Event>
    private var isEditMode: Boolean

    constructor(fragmentManager: FragmentManager, weekList: HashMap<String, ArrayList<LocalDate>>, workplanList: List<Event>, isEditMode: Boolean) : super(fragmentManager) {
        this.weekList = weekList
        this.workplanList = workplanList
        this.isEditMode = isEditMode
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {
        var currentPosition = position + 1
        var daysList = ArrayList(weekList.get("Week " + currentPosition))
        return WeeklyWorkplanFragment(daysList, workplanList, isEditMode)
    }

    override fun getCount(): Int {
        return weekList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return weekList.keys.toList().get(position)
    }
}
package com.app.maidi.domains.child_registration

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.app.maidi.R
import org.hisp.dhis.android.sdk.persistence.models.Option

class OptionAdapter : ArrayAdapter<Option> {

    var optionList: List<Option>
    var layoutId: Int = -1

    constructor(context: Context, layoutId: Int, optionList: List<Option>) : super(context, layoutId, optionList) {
        this.layoutId = layoutId
        this.optionList = optionList
    }

    override fun getItem(position: Int): Option {
        return optionList.get(position)
    }

    override fun getCount(): Int {
        return optionList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        }
        var option = optionList.get(position)
        var tvTitle = view!!.findViewById<TextView>(R.id.item)
        tvTitle.setText(option.displayName)
        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        }
        var tvTitle = view!!.findViewById<TextView>(R.id.item)
        tvTitle.setTextColor(Color.TRANSPARENT)
        return view
    }
}
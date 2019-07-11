package com.app.maidi.domains.child_registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.hisp.dhis.android.sdk.persistence.models.OrganUnit

class ChildRegistrationAdapter : ArrayAdapter<OrganUnit> {

    var organUnits: List<OrganUnit>
    var layoutId: Int = -1
    var selectedPosition: Int = -1

    constructor(context: Context, layoutId: Int, organUnits: List<OrganUnit>) : super(context, layoutId){
        this.layoutId = layoutId
        this.organUnits = organUnits
    }

    override fun getItem(position: Int): OrganUnit {
        return organUnits.get(position)
    }

    override fun getCount(): Int {
        return organUnits.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if(convertView == null){
            view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        }
        var organUnit = getItem(position)
        var tvTitle = view!!.findViewById<TextView>(android.R.id.text1)
        tvTitle.setText(organUnit.displayName)
        return view
    }
}
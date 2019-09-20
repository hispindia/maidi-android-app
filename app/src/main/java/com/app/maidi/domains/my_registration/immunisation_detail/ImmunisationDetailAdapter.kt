package com.app.maidi.domains.my_registration.immunisation_detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.models.Vaccine
import org.hisp.dhis.android.sdk.utils.support.DateUtils

class ImmunisationDetailAdapter : RecyclerView.Adapter<ImmunisationDetailAdapter.ImmunisationDetailHolder>{

    var context: Context
    var dataValues: ArrayList<Vaccine>

    constructor(context: Context, dataValues: ArrayList<Vaccine>) {
        this.context = context
        this.dataValues = dataValues
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunisationDetailHolder {
        var viewGroup = LayoutInflater.from(context).inflate(R.layout.item_immunisation_detail, parent, false)
        return ImmunisationDetailHolder(viewGroup)
    }

    override fun getItemCount(): Int {
        return dataValues.size
    }

    override fun onBindViewHolder(holder: ImmunisationDetailHolder, position: Int) {
        if(position % 2 == 0){
            holder.llContainer.setBackgroundColor(context.resources.getColor(R.color.gray_background_color))
        }
        var vaccine = dataValues.get(position)
        holder.tvVaccineName.text = vaccine.dataElement.displayName
        if(!vaccine.isInjected) {
            holder.tvVaccineDate.text = if (vaccine.dueDate != null && !vaccine.dueDate.isEmpty())
                com.app.maidi.utils.DateUtils.convertCalendarToServerString(DateUtils.parseDate(vaccine.dueDate))
            else ""
        }
        holder.cbInjected.isChecked = vaccine.isInjected
    }

    class ImmunisationDetailHolder : RecyclerView.ViewHolder{

        var llContainer: LinearLayout
        var tvVaccineName: TextView
        var tvVaccineDate: TextView
        var cbInjected: CheckBox

        constructor(contentView: View) : super(contentView){
            llContainer = contentView.findViewById(R.id.item_immunisation_detail_ll_container)
            tvVaccineName = contentView.findViewById(R.id.item_immunisation_detail_tv_vaccine_name)
            tvVaccineDate = contentView.findViewById(R.id.item_immunisation_detail_tv_vaccine_date)
            cbInjected = contentView.findViewById(R.id.item_immunisation_detail_cb_checker)
        }

    }
}
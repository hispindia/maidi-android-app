package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.models.Vaccine
import com.app.maidi.utils.Utils

class ImmunisationVaccineAdapter : RecyclerView.Adapter<ImmunisationVaccineAdapter.ImmunisationVaccineHolder>{

    var context: Context
    var vaccineList: List<Vaccine>

    constructor(context: Context, vaccineList: List<Vaccine>){
        this.context = context
        this.vaccineList = vaccineList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunisationVaccineHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_immunisation_vaccine, parent, false)
        return ImmunisationVaccineHolder(contentView)
    }

    override fun getItemCount(): Int {
        return 6
        //return vaccineList.size
    }

    override fun onBindViewHolder(holder: ImmunisationVaccineHolder, position: Int) {
        var vaccine = vaccineList.get(position)
        holder.tvVaccineName.text = vaccine.dataElement.displayName

        if(vaccine.dueDate != null && !vaccine.dueDate.isEmpty()){
            holder.tvVaccineDueDate.text = Utils.convertFromFullDateToSimpleDate(vaccine.dueDate)
            holder.tvVaccineDueDate.visibility = View.VISIBLE
            holder.cbInjected.visibility = View.GONE
        }

        if(vaccine.isInjected){
            holder.cbInjected.isChecked = true
        }
    }

    class ImmunisationVaccineHolder : RecyclerView.ViewHolder{

        var tvVaccineName: TextView
        var tvVaccineDueDate: TextView
        var cbInjected: CheckBox

        constructor(contentView: View) : super(contentView){
            tvVaccineName = contentView.findViewById(R.id.item_immunisation_vaccine_tv_vaccine_name)
            tvVaccineDueDate = contentView.findViewById(R.id.item_immunisation_vaccine_tv_vaccine_due_date)
            cbInjected = contentView.findViewById(R.id.item_immunisation_vaccine_cb_injected)
        }
    }
}
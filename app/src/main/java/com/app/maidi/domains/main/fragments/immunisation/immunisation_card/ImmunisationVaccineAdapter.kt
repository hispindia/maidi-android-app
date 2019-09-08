package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.models.Vaccine
import com.app.maidi.utils.Utils
import org.hisp.dhis.android.sdk.utils.api.ValueType

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
        return vaccineList.size
    }

    override fun onBindViewHolder(holder: ImmunisationVaccineHolder, position: Int) {
        var vaccine = vaccineList.get(position)

        holder.tvVaccineName.text = vaccine.dataElement.displayName

        if(vaccine.dueDate != null && !vaccine.dueDate.isEmpty()){
            if(Utils.isValidDateFollowPattern(vaccine.dueDate))
                holder.tvVaccineDueDate.text = Utils.convertServerDateToLocalDate(vaccine.dueDate)
            else
                holder.tvVaccineDueDate.text = Utils.convertFromFullDateToSimpleDate(vaccine.dueDate)

            holder.tvVaccineDueDate.visibility = View.VISIBLE
            holder.cbInjected.visibility = View.GONE
        }

        if(vaccine.isInjected){
            holder.cbInjected.isChecked = true
            holder.cbInjected.isEnabled = false

            holder.tvVaccineDueDate.visibility = View.GONE
            holder.cbInjected.visibility = View.VISIBLE
        }
    }

    class ImmunisationVaccineHolder : RecyclerView.ViewHolder{

        var llContainer: LinearLayout
        var tvVaccineName: TextView
        var tvVaccineDueDate: TextView
        var cbInjected: CheckBox
        var etExplanation: EditText

        constructor(contentView: View) : super(contentView){
            llContainer = contentView.findViewById(R.id.item_immunisation_vaccine_ll_container)
            tvVaccineName = contentView.findViewById(R.id.item_immunisation_vaccine_tv_vaccine_name)
            tvVaccineDueDate = contentView.findViewById(R.id.item_immunisation_vaccine_tv_vaccine_due_date)
            cbInjected = contentView.findViewById(R.id.item_immunisation_vaccine_cb_injected)
            etExplanation = contentView.findViewById(R.id.item_immunisation_vaccine_et_explanation)
        }
    }
}
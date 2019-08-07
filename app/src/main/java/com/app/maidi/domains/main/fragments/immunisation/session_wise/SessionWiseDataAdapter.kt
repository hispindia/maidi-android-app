package com.app.maidi.domains.main.fragments.immunisation.session_wise

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.utils.Utils

class SessionWiseDataAdapter : RecyclerView.Adapter<SessionWiseDataAdapter.SessionWiseDataHolder>{

    var context: Context
    var immunisationList: List<ImmunisationCard>

    constructor(context: Context, immunisationList : List<ImmunisationCard>){
        this.context = context
        this.immunisationList = immunisationList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionWiseDataHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_session_wise_data, parent, false)
        return SessionWiseDataHolder(contentView)
    }

    override fun getItemCount(): Int {
        return immunisationList.size
    }

    override fun onBindViewHolder(holder: SessionWiseDataHolder, position: Int) {
        var immunisationCard = immunisationList.get(position)

        if(position % 2 == 0)
            holder.llContainer.setBackgroundColor(context.resources.getColor(R.color.lighter_gray_background_color))
        else
            holder.llContainer.setBackgroundColor(context.resources.getColor(android.R.color.white))

        holder.tvCounter.text = (position + 1).toString()
        if(immunisationCard.trackedEntityInstance != null && immunisationCard.trackedEntityInstance.attributes != null) {
            var attributes = immunisationCard.trackedEntityInstance.attributes
            for(attribute in attributes){
                if(attribute.displayName.contains("Name")){
                    holder.tvChildName.text = attribute.value
                }
            }

        }

        immunisationCard.enrollment!!.let {
            holder.tvDateOfBirth.text = Utils.convertFromFullDateToSimpleDate(it.incidentDate)
        }
        holder.tvRegId.text = immunisationCard.trackedEntityInstance.uid

        getVaccineList(immunisationCard, holder)
    }

    fun getVaccineList(immunisationCard: ImmunisationCard, holder: SessionWiseDataHolder){
        if(immunisationCard.vaccineList != null && immunisationCard.vaccineList.size > 0){
            for(vaccine in immunisationCard.vaccineList){
                var itemView = LayoutInflater.from(context).inflate(R.layout.item_session_vaccine, null)
                var tvVaccineName = itemView.findViewById<TextView>(R.id.item_session_vaccine_tv_vaccine_name)
                var cbChecker = itemView.findViewById<CheckBox>(R.id.item_session_vaccine_cb_checker)
                tvVaccineName.visibility = View.INVISIBLE
                tvVaccineName.text = vaccine.dataElement.displayName
                if(vaccine.isInjected){
                    cbChecker.visibility = View.VISIBLE
                }
                holder.llVaccineList.addView(itemView)
            }

        }

    }

    class SessionWiseDataHolder : RecyclerView.ViewHolder{

        var llContainer: LinearLayout
        var tvCounter: TextView
        var tvChildName: TextView
        var tvDateOfBirth: TextView
        var tvRegId: TextView
        var llVaccineList: LinearLayout

        constructor(contentView: View) : super(contentView){
            llContainer = contentView.findViewById(R.id.item_session_wise_data_ll_container)
            tvCounter = contentView.findViewById(R.id.item_session_wise_data_tv_counter)
            tvChildName = contentView.findViewById(R.id.item_session_wise_data_tv_child_name)
            tvDateOfBirth = contentView.findViewById(R.id.item_session_wise_data_tv_date_of_birth)
            tvRegId = contentView.findViewById(R.id.item_session_wise_data_tv_reg_id)
            llVaccineList = contentView.findViewById(R.id.item_session_wise_data_ll_vaccine)
        }
    }
}
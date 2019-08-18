package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.domains.main.fragments.immunisation.immunisation_card.listener.OnItemClickListener
import com.app.maidi.models.ImmunisationCard
import com.app.maidi.models.Vaccine
import com.app.maidi.utils.Utils
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder

class ImmunisationCardAdapter : RecyclerView.Adapter<ImmunisationCardAdapter.ImmunisationHeaderHolder>, OnItemClickListener{

    var context: Context
    var immunisationList: List<ImmunisationCard>
    var selectPosition = -1

    constructor(context: Context, immunisationList: List<ImmunisationCard>) {
        this.context = context
        this.immunisationList = immunisationList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunisationHeaderHolder {
        var groupView = LayoutInflater.from(context).inflate(R.layout.item_immunisation_card, parent, false)
        return ImmunisationHeaderHolder(context, groupView)
    }

    override fun getItemCount(): Int {
        return immunisationList.size
    }

    override fun onBindViewHolder(holder: ImmunisationHeaderHolder, position: Int) {
        var immunisationCard = immunisationList.get(position)
        holder!!.let {

            holder.setOnClickListener(this)

            if(position % 2 == 0)
                holder.llHeader.setBackgroundColor(context.resources.getColor(R.color.lighter_gray_background_color))
            else
                holder.llHeader.setBackgroundColor(context.resources.getColor(android.R.color.white))

            holder.tvCounter.text = (position + 1).toString()
            //holder.tvRegId.text = immunisationCard.trackedEntityInstance.uid
            if(immunisationCard.trackedEntityInstance != null && immunisationCard.trackedEntityInstance.attributes != null) {
                var attributes = immunisationCard.trackedEntityInstance.attributes
                for(attribute in attributes){
                    if(attribute.displayName.contains("Name")){
                        holder.tvChildName.text = attribute.value
                    }
                }

            }

            immunisationCard.enrollment!!.let {
                holder.tvDob.text = Utils.convertFromFullDateToSimpleDate(it.incidentDate)
            }
            holder.tvRegId.text = immunisationCard.trackedEntityInstance.uid

            if(immunisationCard.isShowContent){
                holder.llVaccineContainer.visibility = View.VISIBLE
            }else{
                holder.llVaccineContainer.visibility = View.GONE
            }

            holder.addDataAndCreateAdapter(immunisationCard.vaccineList)
        }
    }

    override fun onItemClicked(position: Int) {
        showHideItem(position)
        selectPosition = position
    }

    fun showHideItem(position: Int){
        for(item in immunisationList){
            item.isShowContent = false
        }
        if(immunisationList.get(position) != null) {
            immunisationList.get(position).isShowContent = true
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int) : ImmunisationCard?{
        return if(immunisationList != null) immunisationList.get(position) else null
    }

    class ImmunisationHeaderHolder : GroupViewHolder {

        lateinit var adapter: ImmunisationVaccineAdapter
        lateinit var listener: OnItemClickListener

        var context: Context
        var llHeader: LinearLayout
        var llVaccineContainer: LinearLayout
        var tvCounter: TextView
        var tvChildName: TextView
        var tvDob: TextView
        var tvRegId: TextView
        var rcvVaccineList: RecyclerView

        constructor(context: Context, contentView: View) : super(contentView){
            this.context = context
            llHeader = contentView.findViewById(R.id.item_immunisation_card_ll_header)
            llVaccineContainer = contentView.findViewById(R.id.item_immunisation_card_ll_vaccine_container)
            tvCounter = contentView.findViewById(R.id.item_immunisation_card_tv_counter)
            tvChildName = contentView.findViewById(R.id.item_immunisation_card_tv_child_name)
            tvDob = contentView.findViewById(R.id.item_immunisation_card_tv_dob)
            tvRegId = contentView.findViewById(R.id.item_immunisation_vaccine_tv_reg_id)
            rcvVaccineList = contentView.findViewById(R.id.item_immunisation_card_rcv_vaccine_list)

            llHeader.setOnClickListener {
                listener.onItemClicked(layoutPosition)
            }
        }

        fun setOnClickListener(listener: OnItemClickListener){
            this.listener = listener
        }

        fun addDataAndCreateAdapter(vaccineList: List<Vaccine>){
            adapter = ImmunisationVaccineAdapter(context, vaccineList)
            rcvVaccineList.layoutManager = LinearLayoutManager(context)
            rcvVaccineList.adapter = adapter
        }
    }

}
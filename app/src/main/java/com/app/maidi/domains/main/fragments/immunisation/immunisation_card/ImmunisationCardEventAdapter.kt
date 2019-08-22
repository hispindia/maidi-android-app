package com.app.maidi.domains.main.fragments.immunisation.immunisation_card

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.Constants
import com.app.maidi.utils.Utils
import kotlinx.android.synthetic.main.item_immunisation_card_event.view.*
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.persistence.models.Event

class ImmunisationCardEventAdapter : RecyclerView.Adapter<ImmunisationCardEventAdapter.ImmunisationCardEventHolder> {

    var context: Context
    var events: List<Event>
    var listener: OnItemClickListener

    constructor(context: Context, events : List<Event>, listener: OnItemClickListener){
        this.context = context
        this.events = events
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunisationCardEventHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_immunisation_card_event, parent, false)
        return ImmunisationCardEventHolder(contentView)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ImmunisationCardEventHolder, position: Int) {
        var event = events.get(position)
        holder.tvCounter.text = (position + 1).toString()
        holder.setOnClickListener(listener)
        try {
            if (event.organisationUnitId != null) {
                var organUnit = MetaDataController.getOrganisationUnit(event.organisationUnitId)
                holder.tvOrganUnit.text = organUnit.label
            }

            if (event.dueDate != null) {
                var dueDate: String
                if(Utils.isValidDateFollowPattern(event.dueDate)) {
                    dueDate = event.dueDate
                } else {
                    dueDate = Utils.convertFromFullDateToSimpleDate(event.dueDate)
                }
                holder.tvDueDate.text = dueDate
            }
        }catch(ex : Exception){
            Log.e(this::class.simpleName, ex.message)
        }
    }

    class ImmunisationCardEventHolder : RecyclerView.ViewHolder {

        var llHeader: LinearLayout
        var tvOrganUnit: TextView
        var tvCounter: TextView
        var tvDueDate: TextView

        lateinit var listener: OnItemClickListener

        constructor(contentView: View) : super(contentView){
            llHeader = contentView.item_immunisation_card_event_ll_header
            tvCounter = contentView.item_immunisation_card_event_tv_counter
            tvOrganUnit = contentView.item_immunisation_card_event_tv_organ_unit
            tvDueDate = contentView.item_immunisation_card_event_tv_due_date

            llHeader.setOnClickListener {
                listener.onItemClicked(layoutPosition)
            }
        }

        fun setOnClickListener(listener: OnItemClickListener){
            this.listener = listener
        }
    }
}
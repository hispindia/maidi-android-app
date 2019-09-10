package com.app.maidi.domains.main.fragments.workplan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.DateUtils
import kotlinx.android.synthetic.main.item_workplan_event.view.*
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController

class ListVillageAdapter : RecyclerView.Adapter<ListVillageAdapter.ListWorkplanViewHolder> {

    var context: Context
    var eventsMap : HashMap<Long, String>
    var listener: OnItemClickListener

    constructor(context: Context, eventsMap: HashMap<Long, String>, listener: OnItemClickListener){
        this.context = context
        this.eventsMap = eventsMap
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListWorkplanViewHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_workplan_event, parent, false)
        return ListWorkplanViewHolder(contentView, listener)
    }

    override fun getItemCount(): Int {
        return eventsMap.size
    }

    override fun onBindViewHolder(holder: ListWorkplanViewHolder, position: Int) {
        var eventId = eventsMap.keys.toList().get(position)
        var organUnitName = eventsMap.get(eventId)
        var event = TrackerController.getEvent(eventId)
        var eventDate: String?

        if(DateUtils.isValidDateFollowPattern(event.eventDate))
            eventDate = DateUtils.convertServerDateToLocalDate(event.eventDate)
        else
            eventDate = DateUtils.convertFromFullDateToSimpleDate(event.eventDate)

        if(position % 2 == 0)
            holder.llHeader.setBackgroundColor(context.resources.getColor(R.color.lighter_gray_background_color))
        else
            holder.llHeader.setBackgroundColor(context.resources.getColor(android.R.color.white))

        holder.tvCounter.text = (position + 1).toString()
        holder.tvOrganUnit.text = organUnitName
        holder.tvDueDate.text = eventDate
    }

    class ListWorkplanViewHolder : RecyclerView.ViewHolder {

        var llHeader: LinearLayout
        var tvOrganUnit: TextView
        var tvCounter: TextView
        var tvDueDate: TextView

        constructor(contentView: View, listener: OnItemClickListener) : super(contentView){
            this.llHeader = contentView.item_workplan_event_ll_header
            this.tvOrganUnit = contentView.item_workplan_event_tv_organ_unit
            this.tvCounter = contentView.item_workplan_event_tv_counter
            this.tvDueDate = contentView.item_workplan_event_tv_due_date

            this.llHeader.setOnClickListener {
                listener.onItemClicked(layoutPosition)
            }
        }
    }
}
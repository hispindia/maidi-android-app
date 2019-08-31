package com.app.maidi.domains.main.fragments.survey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.domains.main.fragments.listener.OnSurveyItemClickListener
import kotlinx.android.synthetic.main.item_survey.view.*
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class ListSurveyAdapter : RecyclerView.Adapter<ListSurveyAdapter.ListSurveyViewHolder> {

    var context: Context
    var events: List<Event>
    var listener: OnSurveyItemClickListener

    constructor(context: Context, events: List<Event>, listener: OnSurveyItemClickListener){
        this.context = context
        this.events = events
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSurveyViewHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_survey, parent, false)
        return ListSurveyViewHolder(contentView)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ListSurveyViewHolder, position: Int) {
        var event = events.get(position)

        if(position % 2 == 0)
            holder.llContainer.setBackgroundColor(context.resources.getColor(R.color.lighter_gray_background_color))
        else
            holder.llContainer.setBackgroundColor(context.resources.getColor(android.R.color.white))

        holder.tvTitle.text = "Form " + (position + 1)
        holder.ivView.setOnClickListener {
            listener.onViewButtonClicked(event.localId)
        }
        holder.ivExport.setOnClickListener {
            listener.onExportButtonClicked(event.localId)
        }
    }

    class ListSurveyViewHolder : RecyclerView.ViewHolder {

        var llContainer: LinearLayout
        var tvTitle : TextView
        var ivView : ImageView
        var ivExport: ImageView

        constructor(contentView: View) : super(contentView){
            this.llContainer = contentView.item_survey_ll_container
            this.tvTitle = contentView.item_survey_tv_title
            this.ivView = contentView.item_survey_iv_view
            this.ivExport = contentView.item_survey_iv_export
        }
    }
}
package com.app.maidi.domains.my_registration.list_my_registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.utils.DateUtils
import de.hdodenhof.circleimageview.CircleImageView
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class ListMyRegistrationAdapter : RecyclerView.Adapter<ListMyRegistrationAdapter.ListMyRegistrationHolder> {

    var context: Context
    var programId: String
    var trackedEntityInstances: List<TrackedEntityInstance>
    var listener: OnItemClickListener

    constructor(context: Context, programId: String, trackedEntityInstances: List<TrackedEntityInstance>, listener: OnItemClickListener) {
        this.context = context
        this.programId = programId
        this.trackedEntityInstances = trackedEntityInstances
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListMyRegistrationHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_my_registration, parent, false)
        return ListMyRegistrationHolder(contentView, listener)
    }

    override fun getItemCount(): Int {
        return trackedEntityInstances.size
    }

    override fun onBindViewHolder(holder: ListMyRegistrationHolder, position: Int) {
        var trackedInstance = trackedEntityInstances.get(position)
        var name = ""
        var mother = ""
        var stringBuilder = StringBuilder()

        var enrollment = TrackerController.getEnrollment(programId, trackedInstance)

        if (trackedInstance.attributes != null) {
            for (attribute in trackedInstance.attributes) {

                if (attribute.displayName.contains("Mother")) {
                    mother = attribute.value
                    continue
                }

                if (attribute.displayName.contains("Name")) {
                    name = attribute.value
                    continue
                }
            }
        }

        if(enrollment != null && enrollment.incidentDate != null) {
            if (DateUtils.isValidDateFollowPattern(enrollment.incidentDate)) {
                stringBuilder.append(enrollment.incidentDate)
            } else {
                stringBuilder.append(DateUtils.convertFromFullDateToSimpleDate(enrollment.incidentDate))
            }
        }

        if (!mother.isEmpty())
            stringBuilder.append(" / " + mother)

        holder.tvName.text = name
        holder.tvDescription.text = stringBuilder.toString()
    }

    class ListMyRegistrationHolder : RecyclerView.ViewHolder {

        var flContent: FrameLayout
        var flBackground: FrameLayout
        var civAvatar: CircleImageView
        var tvName: TextView
        var tvDescription: TextView

        constructor(contentView: View, listener: OnItemClickListener) : super(contentView) {
            flContent = contentView.findViewById(R.id.item_my_registration_fl_content)
            flBackground = contentView.findViewById(R.id.item_my_registration_fl_background)
            civAvatar = contentView.findViewById(R.id.item_my_registration_civ_avatar)
            tvName = contentView.findViewById(R.id.item_my_registration_tv_name)
            tvDescription = contentView.findViewById(R.id.item_my_registration_tv_description)

            flContent.setOnClickListener({
                listener.onItemClicked(layoutPosition)
            })
        }

    }

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }
}
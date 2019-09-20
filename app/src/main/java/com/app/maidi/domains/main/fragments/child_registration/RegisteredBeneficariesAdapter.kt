package com.app.maidi.domains.main.fragments.child_registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.DateUtils
import de.hdodenhof.circleimageview.CircleImageView
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class RegisteredBeneficariesAdapter : RecyclerView.Adapter<RegisteredBeneficariesAdapter.RegisteredBeneficariesHolder> {

    var context: Context
    var trackedEntityInstances: List<TrackedEntityInstance>
    var listener: OnItemClickListener

    constructor(context: Context, trackedEntityInstances: List<TrackedEntityInstance>, listener: OnItemClickListener) {
        this.context = context
        this.trackedEntityInstances = trackedEntityInstances
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisteredBeneficariesHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_my_registration, parent, false)
        return RegisteredBeneficariesHolder(contentView, listener)
    }

    override fun getItemCount(): Int {
        return trackedEntityInstances.size
    }

    override fun onBindViewHolder(holder: RegisteredBeneficariesHolder, position: Int) {
        var trackedInstance = trackedEntityInstances.get(position)
        var name = ""
        var mother = ""
        var stringBuilder = StringBuilder()

        if (trackedInstance.attributes != null) {
            for (attribute in trackedInstance.attributes) {

                if (attribute.displayName.contains("Mother")) {
                    mother = attribute.value
                    continue
                }

                if (attribute.displayName.contains("Date of Birth")) {
                    if(attribute.value != null) {
                        if (DateUtils.isValidDateFollowPattern(attribute.value)) {
                            stringBuilder.append(attribute.value)
                        } else {
                            stringBuilder.append(DateUtils.convertFromFullDateToSimpleDate(attribute.value))
                        }
                    }
                    continue
                }

                if (attribute.displayName.contains("Name")) {
                    name = attribute.value
                    continue
                }
            }
        }

        if (!mother.isEmpty())
            if(!stringBuilder.isEmpty())
                stringBuilder.append(" - " + mother)
            else
                stringBuilder.append(mother)

        holder.tvName.text = name
        holder.tvDescription.text = stringBuilder.toString()
    }

    class RegisteredBeneficariesHolder : RecyclerView.ViewHolder {

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
}
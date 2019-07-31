package com.app.maidi.domains.main.fragments.aefi

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.utils.Constants
import com.app.maidi.utils.Utils
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class RegisteredCaseAdapter : RecyclerView.Adapter<RegisteredCaseAdapter.RegisteredCaseHolder>{

    var context: Context
    var trackedEntityInstances: List<TrackedEntityInstance>

    constructor(context: Context, trackedEntityInstances: List<TrackedEntityInstance>){
        this.context = context
        this.trackedEntityInstances = trackedEntityInstances
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisteredCaseHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_registered_cases, parent, false)
        return RegisteredCaseHolder(contentView)
    }

    override fun getItemCount(): Int {
        return trackedEntityInstances.size
    }

    override fun onBindViewHolder(holder: RegisteredCaseHolder, position: Int) {
        try {
            var trackedEntityInstance = trackedEntityInstances.get(position)

            if(position % 2 == 0)
                holder.llHeader.setBackgroundColor(context.resources.getColor(R.color.lighter_gray_background_color))
            else
                holder.llHeader.setBackgroundColor(context.resources.getColor(android.R.color.white))

            holder.tvCounter.text = (position + 1).toString()
            holder.tvRegId.text = trackedEntityInstance.uid
            holder.tvRegDate.text = Utils.convertFromFullDateToSimpleDate(trackedEntityInstance.created)

            var attributes = trackedEntityInstance.attributes
            for (attribute in attributes) {
                if (attribute.displayName.contains("Name")) {
                    holder.tvChildName.text = attribute.value
                }

                if (attribute.displayName.contains("Date of Birth")) {
                    if(Utils.isValidDateFollowPattern(Constants.SERVER_DATE_PATTERN, attribute.value)) {
                        holder.tvDob.text = attribute.value
                    } else {
                        holder.tvDob.text = Utils.convertFromFullDateToSimpleDate(attribute.value)
                    }
                }

                if (attribute.displayName.contains("Gender")) {
                    var trackedEntityAttribute =
                        MetaDataController.getTrackedEntityAttribute(attribute.trackedEntityAttributeId)
                    var options = MetaDataController.getOptions(trackedEntityAttribute.optionSet)
                    for (option in options) {
                        if (option.code.equals(attribute.value)) {
                            holder.tvGender.text = option.displayName
                            break
                        }
                    }
                }
            }

            holder.llHeader.setOnClickListener {
                Utils.showHideContainer(holder.llChildInfo, 500)
            }

        }catch(exception : Exception){
            Log.e(RegisteredCaseAdapter::class.simpleName, exception.toString())
        }

    }

    class RegisteredCaseHolder : RecyclerView.ViewHolder{

        var llHeader: LinearLayout
        var llChildInfo: LinearLayout
        var tvCounter: TextView
        var tvChildName: TextView
        var tvDob: TextView
        var tvRegId: TextView
        var tvRegDate: TextView
        var tvGender: TextView

        constructor(contentView: View) : super(contentView){
            llHeader = contentView.findViewById(R.id.item_registered_cases_ll_header)
            llChildInfo = contentView.findViewById(R.id.item_registered_cases_ll_child_info_container)
            tvCounter = contentView.findViewById(R.id.item_registered_cases_tv_counter)
            tvChildName = contentView.findViewById(R.id.item_registered_cases_tv_child_name)
            tvDob = contentView.findViewById(R.id.item_registered_cases_tv_dob)
            tvRegId = contentView.findViewById(R.id.item_registered_cases_tv_reg_id)
            tvRegDate = contentView.findViewById(R.id.item_registered_cases_tv_reg_date)
            tvGender = contentView.findViewById(R.id.item_registered_cases_tv_gender)
        }
    }
}
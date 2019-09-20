package com.app.maidi.domains.main.fragments.aefi

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import com.app.maidi.domains.main.fragments.listener.OnItemClickListener
import com.app.maidi.utils.Constants
import com.app.maidi.utils.DateUtils
import com.app.maidi.utils.MethodUtils
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance

class RegisteredCaseAdapter : RecyclerView.Adapter<RegisteredCaseAdapter.RegisteredCaseHolder>{

    var activity: AppCompatActivity
    var trackedEntityInstances: List<TrackedEntityInstance>
    var listener: OnItemClickListener

    constructor(activity: AppCompatActivity, trackedEntityInstances: List<TrackedEntityInstance>, listener: OnItemClickListener){
        this.activity = activity
        this.trackedEntityInstances = trackedEntityInstances
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisteredCaseHolder {
        var contentView = LayoutInflater.from(activity).inflate(R.layout.item_registered_cases, parent, false)
        return RegisteredCaseHolder(contentView)
    }

    override fun getItemCount(): Int {
        return trackedEntityInstances.size
    }

    override fun onBindViewHolder(holder: RegisteredCaseHolder, position: Int) {
        try {
            var trackedEntityInstance = trackedEntityInstances.get(position)
            var enrollment = TrackerController.getEnrollment(trackedEntityInstance)
            var aefiProgram = MetaDataController.getProgramByName(Constants.AEFI)

            if(position % 2 == 0)
                holder.llHeader.setBackgroundColor(activity.resources.getColor(R.color.lighter_gray_background_color))
            else
                holder.llHeader.setBackgroundColor(activity.resources.getColor(android.R.color.white))

            holder.tvCounter.text = (position + 1).toString()
            holder.tvRegId.text = trackedEntityInstance.uid
            holder.tvRegDate.text = DateUtils.convertFromFullDateToSimpleDate(trackedEntityInstance.created)

            var attributes = trackedEntityInstance.attributes
            for (attribute in attributes) {
                if (attribute.displayName.contains("Name")) {
                    holder.tvChildName.text = attribute.value
                }

                if(enrollment.program.equals(aefiProgram.uid)) {
                    if (attribute.displayName.contains("Date of Birth")) {
                        if (DateUtils.isValidDateFollowPattern(attribute.value)) {
                            holder.tvDob.text = attribute.value
                        } else {
                            holder.tvDob.text = DateUtils.convertFromFullDateToSimpleDate(attribute.value)
                        }
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

            if(!enrollment.program.equals(aefiProgram.uid)) {
                if(enrollment.incidentDate != null) {
                    holder.tvDob.text = DateUtils.convertCalendarToString(
                        org.hisp.dhis.android.sdk.utils.support.DateUtils.parseDate(enrollment.incidentDate)
                    )
                }
            }

            holder.llHeader.setOnClickListener {
                MethodUtils.showHideContainer(holder.llChildInfo, 500)
            }

            holder.llChildInfo.setOnClickListener {
                listener.onItemClicked(holder.layoutPosition)
            }

        }catch(exception : Exception){
            Log.e("RegisteredCaseAdapter", exception.toString())
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
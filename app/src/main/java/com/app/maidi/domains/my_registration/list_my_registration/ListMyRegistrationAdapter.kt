package com.app.maidi.domains.my_registration.list_my_registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.maidi.R
import de.hdodenhof.circleimageview.CircleImageView
import org.hisp.dhis.android.sdk.persistence.models.Enrollment

class ListMyRegistrationAdapter : RecyclerView.Adapter<ListMyRegistrationAdapter.ListMyRegistrationHolder> {

     var context: Context
     var registrationList: ArrayList<Enrollment>

    constructor(context: Context, registrationList: ArrayList<Enrollment>){
        this.context = context
        this.registrationList = registrationList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListMyRegistrationHolder {
        var contentView = LayoutInflater.from(context).inflate(R.layout.item_my_registration, parent, false)
        return ListMyRegistrationHolder(contentView)
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: ListMyRegistrationHolder, position: Int) {

    }

    class ListMyRegistrationHolder : RecyclerView.ViewHolder {

         var flBackground : FrameLayout
         var civAvatar: CircleImageView
         var tvName: TextView
         var tvDescription: TextView
         var tvTime: TextView

        constructor(contentView: View) : super(contentView){
            flBackground = contentView.findViewById(R.id.item_my_registration_fl_background)
            civAvatar = contentView.findViewById(R.id.item_my_registration_civ_avatar)
            tvName = contentView.findViewById(R.id.item_my_registration_tv_name)
            tvDescription = contentView.findViewById(R.id.item_my_registration_tv_description)
            tvTime = contentView.findViewById(R.id.item_my_registration_tv_time)
        }

    }
}
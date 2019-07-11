package com.app.maidi.domains.my_registration.immunisation_detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ImmunisationDetailAdapter : RecyclerView.Adapter<ImmunisationDetailAdapter.ImmunisationDetailHolder>{

    lateinit var context: Context
    //lateinit var immunisationList: ArrayList<>

    constructor(){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunisationDetailHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: ImmunisationDetailHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class ImmunisationDetailHolder : RecyclerView.ViewHolder{

        constructor(contentView: View) : super(contentView){

        }

    }
}
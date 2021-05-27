package com.parita.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.parita.chatapplication.R
import com.parita.chatapplication.model.Help

class HelpAdapter(helpList: ArrayList<Help>) : RecyclerView.Adapter<HelpAdapter.MyViewHolder>() {
    private val helpList: ArrayList<Help>
    private val TAG = HelpAdapter::class.java.simpleName

    inner class MyViewHolder(view: View?) : RecyclerView.ViewHolder(
        view!!
    ) {
        var helpText: TextView
        var helpImage: ImageView

        init {
            helpText = itemView.findViewById(R.id.help_name)
            helpImage = itemView.findViewById(R.id.help_img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_help_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val help: Help = helpList[position]
        holder.helpText.text = help.helpText
        holder.helpImage.setImageResource(help.imageName)
    }

    override fun getItemCount(): Int {
        return helpList.size
    }

    init {
        this.helpList = helpList
    }
}

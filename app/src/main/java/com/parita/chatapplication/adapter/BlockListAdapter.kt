package com.parita.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.parita.chatapplication.R
import com.parita.chatapplication.model.Friends
import java.util.*

class BlockListAdapter(friends: ArrayList<Friends>) :
    RecyclerView.Adapter<BlockListAdapter.HolderClass>() {
    private val friends: ArrayList<Friends>
    private lateinit var onContainerClicked: onCClicked
    var position = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderClass {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_block_list_layout, parent, false)
        return HolderClass(view)
    }

    override fun onBindViewHolder(holder: HolderClass, position: Int) {
        holder.message.setText(friends[position].email)
        holder.container.setOnClickListener { v ->
            this.position = position
            onContainerClicked.onClick(v)
        }
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    inner class HolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView
        var container: ConstraintLayout

        init {
            container = itemView.findViewById(R.id.container)
            message = itemView.findViewById(R.id.text)
        }
    }

    interface onCClicked : View.OnClickListener {
        override fun onClick(v: View)
    }

    fun clickListenerAction(listener: onCClicked) {
        onContainerClicked = listener
    }

    init {
        this.friends = friends
    }
}

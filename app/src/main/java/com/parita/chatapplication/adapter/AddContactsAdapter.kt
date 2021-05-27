package com.parita.chatapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.parita.chatapplication.R
import com.parita.chatapplication.model.User
import java.util.*

class AddContactsAdapter(userList: ArrayList<User>) :
    RecyclerView.Adapter<AddContactsAdapter.HolderClass>() {
    private val userList: ArrayList<User>
    private lateinit var onContainerClicked: onCClicked
    var position = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderClass {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_addcontacts_layout, parent, false)
        return HolderClass(view)
    }

    override fun onBindViewHolder(holder: HolderClass, position: Int) {
        if (userList[position].isAccountStatus) {
            holder.email.setText(userList[position].email)
            if (!userList[position].isLoginStatus) {
                holder.status.setImageResource(R.drawable.ic_user_status_unavailable)
            }
            holder.container.setOnClickListener { v ->
                this.position = position
                onContainerClicked.onClick(v)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class HolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var email: TextView
        var dp: ImageView
        var status: ImageView
        var container: ConstraintLayout

        init {
            email = itemView.findViewById(R.id.text)
            dp = itemView.findViewById(R.id.image)
            status = itemView.findViewById(R.id.status)
            container = itemView.findViewById(R.id.container)
        }
    }

    interface onCClicked : View.OnClickListener {
        override fun onClick(v: View)
    }

    fun clickListenerAction(listener: onCClicked) {
        onContainerClicked = listener
    }

    init {
        this.userList = userList
    }
}

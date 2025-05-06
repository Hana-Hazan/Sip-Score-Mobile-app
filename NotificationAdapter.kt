// ✅ NotificationAdapter.kt – הצגת טקסט התראה דינמי לפי סוג (לייק על פוסט או תגובה)
package com.example.sipscore.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.models.NotificationItem

class NotificationAdapter(private val notifications: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.likerProfileImage)
        val notificationText: TextView = view.findViewById(R.id.likeText)
        val postImage: ImageView = view.findViewById(R.id.postThumbnail)
        val divider: View = view.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        val context = holder.itemView.context

        val profileResId = context.resources.getIdentifier(item.likerProfileImage, "drawable", context.packageName)
        val postResId = context.resources.getIdentifier(item.postImage, "drawable", context.packageName)

        holder.profileImage.setImageResource(
            if (profileResId != 0) profileResId else R.drawable.default_profile
        )
        holder.postImage.setImageResource(
            if (postResId != 0) postResId else R.drawable.default_post
        )
        holder.notificationText.text = item.message
        holder.postImage.alpha = 0.8f
        holder.divider.visibility = if (position < notifications.size - 1) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = notifications.size
}

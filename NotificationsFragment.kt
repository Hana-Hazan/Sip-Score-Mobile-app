package com.example.sipscore.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.adapters.NotificationAdapter
import com.example.sipscore.models.NotificationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsFragment : Fragment() {

    private lateinit var currentUserEmail: String
    private lateinit var recyclerView: RecyclerView
    private val notifications = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter

        val sharedPref = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        currentUserEmail = sharedPref.getString("loggedInEmail", null) ?: return

        loadNotifications()
    }

    private fun loadNotifications() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUserEmail)
            .collection("notifications")
            .orderBy("date")
            .get()
            .addOnSuccessListener { result ->
                notifications.clear()
                for (doc in result) {
                    val type = doc.getString("type") ?: continue
                    val fromUsername = doc.getString("fromUsername") ?: "Unknown"
                    val profileImage = doc.getString("fromProfileImage") ?: "default_profile"
                    val date = doc.getString("date") ?: ""

                    val message = when (type) {
                        "like_post" -> "$fromUsername liked your post"
                        "like_comment" -> "$fromUsername liked your comment"
                        else -> "$fromUsername sent you a notification"
                    }

                    val item = NotificationItem(
                        likerUsername = fromUsername,
                        likerProfileImage = profileImage,
                        postImage = "default_post", // ניתן להרחיב בעתיד לתמונות תגובה
                        message = message,
                        date = date
                    )
                    notifications.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }
}

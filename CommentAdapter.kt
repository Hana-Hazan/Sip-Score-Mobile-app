// ✅ CommentAdapter.kt – מעודכן כולל תמיכה בלייקים מוגבלים ומספר לייקים
package com.example.sipscore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val commentContentTextView: TextView = view.findViewById(R.id.commentContentTextView)
        val thumbsUp: ImageView = view.findViewById(R.id.thumbsUp)
        val thumbsDown: ImageView = view.findViewById(R.id.thumbsDown)
//        val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val context = holder.itemView.context

        holder.usernameTextView.text = comment.commenterUsername
        holder.dateTextView.text = comment.date
        holder.commentContentTextView.text = comment.content

        val profileResId = context.resources.getIdentifier(
            comment.commenterProfileImage, "drawable", context.packageName
        )
        holder.profileImage.setImageResource(
            if (profileResId != 0) profileResId else R.drawable.default_profile
        )

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email ?: return
        val postId = (holder.itemView.parent as? RecyclerView)?.tag as? String ?: return

        val commentId = comment.commentId
        val commentRef = db.collection("posts").document(postId)
            .collection("comments").document(commentId)
        val likeRef = commentRef.collection("likes").document(userEmail)

        likeRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                holder.thumbsUp.setImageResource(R.drawable.ic_like_filled)
            } else {
                holder.thumbsUp.setImageResource(R.drawable.ic_like_outline)
            }
        }

//        commentRef.collection("likes").get().addOnSuccessListener { query ->
//            holder.likeCountTextView.text = query.size().toString()
//        }

        holder.thumbsUp.setOnClickListener {
            likeRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    likeRef.delete()
                    holder.thumbsUp.setImageResource(R.drawable.ic_like_outline)
                    Toast.makeText(context, "Like removed", Toast.LENGTH_SHORT).show()
                } else {
                    likeRef.set(hashMapOf("liked" to true))
                    holder.thumbsUp.setImageResource(R.drawable.ic_like_filled)

                    val notification = hashMapOf(
                        "type" to "like_comment",
                        "fromUser" to userEmail,
                        "targetUsername" to comment.commenterUsername,
                        "timestamp" to System.currentTimeMillis(),
                        "comment" to comment.content
                    )
                    db.collection("notifications").add(notification)
                    Toast.makeText(context, "Liked comment!", Toast.LENGTH_SHORT).show()
                }

//                commentRef.collection("likes").get().addOnSuccessListener { query ->
//                    holder.likeCountTextView.text = query.size().toString()
//                }
            }
        }

        holder.thumbsDown.setOnClickListener {
            Toast.makeText(context, "Dislike feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = comments.size
}

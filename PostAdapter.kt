package com.example.sipscore.adapter

import android.app.Activity
import android.content.Context
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val postText: EditText = itemView.findViewById(R.id.postText)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val likesCountText: TextView = itemView.findViewById(R.id.likesCountText)
        val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        val saveIcon: ImageView = itemView.findViewById(R.id.saveIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)

        // Hide keyboard when tapping outside EditText
        view.setOnTouchListener { v, _ ->
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
            false
        }

        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val context = holder.itemView.context

        holder.usernameText.text = post.username
        holder.dateText.text = post.date
        holder.postText.setText(post.content)
        holder.postText.isFocusable = false
        holder.postText.isFocusableInTouchMode = false
        holder.saveIcon.visibility = View.GONE

        val imageResId = context.resources.getIdentifier(
            post.imageProfile, "drawable", context.packageName
        )
        holder.postImage.setImageResource(
            if (imageResId != 0) imageResId else R.drawable.default_profile
        )

        holder.likesCountText.text = "${post.likes.size}"
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (post.email == currentUserEmail) {
            holder.editIcon.visibility = View.VISIBLE
            holder.deleteIcon.visibility = View.VISIBLE
        } else {
            holder.editIcon.visibility = View.GONE
            holder.deleteIcon.visibility = View.GONE
        }

        holder.editIcon.setOnClickListener {
            holder.postText.isFocusable = true
            holder.postText.isFocusableInTouchMode = true
            holder.postText.requestFocus()

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.postText, InputMethodManager.SHOW_IMPLICIT)

            holder.editIcon.visibility = View.GONE
            holder.saveIcon.visibility = View.VISIBLE
        }

        holder.saveIcon.setOnClickListener {
            val updatedText = holder.postText.text.toString()
            val db = FirebaseFirestore.getInstance()

            db.collection("posts").document(post.id)
                .update("content", updatedText)
                .addOnSuccessListener {
                    postList[position] = post.copy(content = updatedText)
                    holder.postText.isFocusable = false
                    holder.postText.isFocusableInTouchMode = false

                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(holder.postText.windowToken, 0)

                    holder.saveIcon.visibility = View.GONE
                    holder.editIcon.visibility = View.VISIBLE
                    Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show()
                }
        }

        holder.deleteIcon.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            db.collection("posts").document(post.id)
                .delete()
                .addOnSuccessListener {
                    postList.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to delete post: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = postList.size
}

package com.example.sipscore.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.databinding.ItemPostBinding
import com.example.sipscore.models.Post
import android.view.View
import androidx.navigation.Navigation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import android.os.Bundle


class FeedAdapter(
    private val posts: MutableList<Post>,
    private val loggedInEmail: String
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        with(holder.binding) {
            usernameText.text = post.username
            dateText.text = post.date
            postText.setText(post.content)
            postText.isFocusable = false
            postText.isFocusableInTouchMode = false
            saveIcon.visibility = View.GONE

            // תמונת פרופיל
            val profileImageName = post.imageProfile.substringBeforeLast(".")
            val profileResId = context.resources.getIdentifier(profileImageName, "drawable", context.packageName)
            userImage.setImageResource(if (profileResId != 0) profileResId else R.drawable.default_profile)

            // תמונת פוסט
            if (post.image.startsWith("content://")) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(Uri.parse(post.image))
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        withContext(Dispatchers.Main) {
                            postImage.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            postImage.setImageResource(R.drawable.sample_post)
                        }
                    }
                }
            } else {
                val postImageName = post.image.substringBeforeLast(".")
                val postImageResId = context.resources.getIdentifier(postImageName, "drawable", context.packageName)
                postImage.setImageResource(if (postImageResId != 0) postImageResId else R.drawable.sample_post)
            }

            likesCountText.text = post.likes.size.toString()

            // ⚡ לחיצה על אייקון תגובות – מעבר ל־CommentsFragment
            commentIcon.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("postId", post.id)
                }
                val navController = Navigation.findNavController(holder.itemView)
                navController.navigate(R.id.action_feedFragment_to_commentsFragment, bundle)
            }

            if (post.email == loggedInEmail) {
                editIcon.visibility = View.VISIBLE
                deleteIcon.visibility = View.VISIBLE

                editIcon.setOnClickListener {
                    postText.isFocusable = true
                    postText.isFocusableInTouchMode = true
                    postText.requestFocus()

                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(postText, InputMethodManager.SHOW_IMPLICIT)

                    editIcon.visibility = View.GONE
                    saveIcon.visibility = View.VISIBLE
                }

                saveIcon.setOnClickListener {
                    val updatedText = postText.text.toString()
                    val db = FirebaseFirestore.getInstance()

                    db.collection("posts").document(post.id)
                        .update("content", updatedText)
                        .addOnSuccessListener {
                            posts[position] = post.copy(content = updatedText)
                            postText.isFocusable = false
                            postText.isFocusableInTouchMode = false

                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(postText.windowToken, 0)

                            saveIcon.visibility = View.GONE
                            editIcon.visibility = View.VISIBLE
                            Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show()
                        }
                }

                deleteIcon.setOnClickListener {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("posts").document(post.id)
                        .delete()
                        .addOnSuccessListener {
                            posts.removeAt(position)
                            notifyItemRemoved(position)
                            Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to delete post: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                editIcon.visibility = View.GONE
                deleteIcon.visibility = View.GONE
            }
        }
    }
}

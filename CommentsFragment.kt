// ✅ CommentsFragment.kt – יצירת תגובה עם מזהה commentId ייחודי ותמיכה מלאה ב-Firestore
package com.example.sipscore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.adapter.CommentAdapter
import com.example.sipscore.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CommentsFragment : Fragment() {

    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentEditText: EditText
    private lateinit var sendCommentButton: ImageView
    private lateinit var backButton: ImageView

    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var postId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView)
        commentEditText = view.findViewById(R.id.commentEditText)
        sendCommentButton = view.findViewById(R.id.sendCommentButton)
        backButton = view.findViewById(R.id.backButton)

        commentAdapter = CommentAdapter(commentList)
        commentsRecyclerView.adapter = commentAdapter
        commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        postId = arguments?.getString("postId") ?: return
        commentsRecyclerView.tag = postId

        loadCommentsFromFirestore()

        sendCommentButton.setOnClickListener {
            val content = commentEditText.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please write a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userEmail).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "User"
                    val profileImage = document.getString("profileImage") ?: "default_profile"
                    val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

                    val newCommentRef = db.collection("posts").document(postId)
                        .collection("comments").document()
                    val commentId = newCommentRef.id

                    val comment = Comment(
                        commenterUsername = username,
                        commenterProfileImage = profileImage,
                        content = content,
                        date = date,
                        commentId = commentId
                    )

                    newCommentRef.set(comment)
                        .addOnSuccessListener {
                            commentList.add(comment)
                            commentAdapter.notifyItemInserted(commentList.size - 1)
                            commentEditText.text.clear()
                            commentsRecyclerView.scrollToPosition(commentList.size - 1)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to send comment", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun loadCommentsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .collection("comments")
            .get()
            .addOnSuccessListener { result ->
                commentList.clear()
                for (document in result) {
                    val comment = Comment(
                        commenterUsername = document.getString("commenterUsername") ?: "",
                        commenterProfileImage = document.getString("commenterProfileImage") ?: "default_profile",
                        content = document.getString("content") ?: "",
                        date = document.getString("date") ?: "",
                        commentId = document.id 
                    )
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.sipscore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.adapter.FeedAdapter
import com.example.sipscore.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private val postList = mutableListOf<Post>()

    val newPostId = arguments?.getString("newPostId")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.feedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val loggedInEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        feedAdapter = FeedAdapter(postList, loggedInEmail)
        recyclerView.adapter = feedAdapter

        // קבלת מזהה פוסט חדש אם קיים
        val newPostId = arguments?.getString("newPostId")

        // קריאת פוסטים כולל מיון לפי פוסט חדש
        loadPostsFromFirestore(newPostId)

    }

    private fun loadPostsFromFirestore(newPostId: String? = null) {
        FirebaseFirestore.getInstance().collection("posts")
            .get()
            .addOnSuccessListener { result ->
                postList.clear()
                for (document in result) {
                    val post = Post(
                        id = document.id,
                        image = document.getString("image")?.removeSuffix(".png")?.removeSuffix(".jpg") ?: "",
                        username = document.getString("username") ?: "",
                        imageProfile = document.getString("imageProfile") ?: "",
                        content = document.getString("content") ?: "",
                        date = document.getString("date") ?: "",
                        email = document.getString("email") ?: ""
                    )
                    postList.add(post)
                }

                // אם קיים פוסט חדש – העבר אותו לראש
                if (!newPostId.isNullOrEmpty()) {
                    val index = postList.indexOfFirst { it.id == newPostId }
                    if (index > 0) {
                        val post = postList.removeAt(index)
                        postList.add(0, post)
                    }
                }

                feedAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { it.printStackTrace() }
    }
}

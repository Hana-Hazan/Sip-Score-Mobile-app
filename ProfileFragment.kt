package com.example.sipscore.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.adapter.PostAdapter
import com.example.sipscore.models.Post
import com.example.sipscore.utils.JsonUtils
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var bannerImage: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var favoritesIcon: ImageView
    private lateinit var usernameText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var userPosts: List<Post>
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // קישור רכיבי UI
        profileImage = view.findViewById(R.id.profileImage)
        bannerImage = view.findViewById(R.id.bannerImage)
        settingsIcon = view.findViewById(R.id.settingsIcon)
        favoritesIcon = view.findViewById(R.id.favoritesIcon)
        usernameText = view.findViewById(R.id.usernameText)
        recyclerView = view.findViewById(R.id.profileRecyclerView)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // 💡 עיכוב של שנייה לטעינה חלקה
        Handler(Looper.getMainLooper()).postDelayed({

            val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val loggedInEmail = sharedPrefs.getString("loggedInEmail", null)
            val users = JsonUtils.loadUserAccounts(requireContext())
            val user = loggedInEmail?.let { users[it] }

            if (user != null) {
                val firstName = user.firstName
                val lastInitial = user.lastName.firstOrNull()?.uppercase() ?: ""
                val shortUsername = "$firstName$lastInitial"

                usernameText.text = shortUsername

                user.profileImage?.let { imageName ->
                    val file = File(requireContext().filesDir, imageName)
                    if (file.exists()) {
                        profileImage.setImageURI(Uri.fromFile(file))
                    }
                }
            } else {
                usernameText.text = "Guest"
            }

            if (loggedInEmail != null) {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                userPosts = JsonUtils.loadUserPosts(requireContext(), loggedInEmail)
                postAdapter = PostAdapter(userPosts.toMutableList())
                recyclerView.adapter = postAdapter
            }

        }, 1000) // 1000 מילי־שניות = שנייה אחת

        settingsIcon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        favoritesIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Moving to Favorites screen...", Toast.LENGTH_SHORT).show()
        }
    }
}

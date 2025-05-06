package com.example.sipscore.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sipscore.R
import com.example.sipscore.utils.hideKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.sipscore.MainActivity

class NewPostFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var postContentEditText: EditText
    private lateinit var uploadImageButton: ImageButton
    private lateinit var selectedImageView: ImageView
    private lateinit var postButton: Button

    private var selectedImageName: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var editingPostId: String? = null // מזהה הפוסט לעריכה
    private var editingImageName: String? = null // שם תמונה לעריכה

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_new_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.userProfileImage)
        usernameText = view.findViewById(R.id.usernameText)
        postContentEditText = view.findViewById(R.id.postContentEditText)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        selectedImageView = view.findViewById(R.id.selectedImageView)
        postButton = view.findViewById(R.id.postButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // טען שם משתמש ותמונת פרופיל
        val email = auth.currentUser?.email
        if (email != null) {
            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "User"
                    val profileImageName = document.getString("profileImage") ?: "default_profile"

                    usernameText.text = username
                    val resId = resources.getIdentifier(profileImageName, "drawable", requireContext().packageName)
                    if (resId != 0) {
                        profileImage.setImageResource(resId)
                    }
                }
        }

        // עריכה קיימת?
        editingPostId = arguments?.getString("postId")
        val postText = arguments?.getString("postContent")
        val postImage = arguments?.getString("postImageUri")

        if (editingPostId != null && postText != null && postImage != null) {
            postContentEditText.setText(postText)
            selectedImageName = postImage
            editingImageName = postImage
            postButton.text = "Update"
            view.findViewById<TextView>(R.id.titleText)?.text = "Edit Post"

            val resId = resources.getIdentifier(postImage, "drawable", requireContext().packageName)
            if (resId != 0) {
                selectedImageView.setImageResource(resId)
            }
        }

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1001)
        }

        postButton.setOnClickListener {
            val content = postContentEditText.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please write something", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userEmail = auth.currentUser?.email ?: return@setOnClickListener

            db.collection("users").document(userEmail).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageName = document.getString("profileImage") ?: "default_profile"
                    val imageName = selectedImageName ?: editingImageName ?: "sample_post"
                    val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
                    val postId = editingPostId ?: UUID.randomUUID().toString()

                    val postMap = hashMapOf(
                        "id" to postId,
                        "image" to imageName,
                        "username" to username,
                        "imageProfile" to profileImageName,
                        "content" to content,
                        "date" to date,
                        "email" to userEmail
                    )

                    db.collection("posts").document(postId)
                        .set(postMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Post uploaded!", Toast.LENGTH_SHORT).show()
                            resetForm()

                            // ניווט למסך Feed עם פרמטר postId
                            val bundle = Bundle().apply {
                                putString("newPostId", postId)
                            }
//                            findNavController().navigate(R.id.action_newPostFragment_to_feedFragment, bundle)
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.putExtra("newPostId", postId)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        view.setOnTouchListener { v, _ ->
            hideKeyboard(requireActivity())
            v.performClick()
            false
        }
    }

    private fun createOrUpdatePost() {
        val content = postContentEditText.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Please write something", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        val email = currentUser?.email ?: return Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()

        db.collection("users").document(email).get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Unknown"
                val profileImageName = document.getString("profileImage") ?: "default_profile"
                val imageName = selectedImageName ?: editingImageName ?: "sample_post"
                val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
                val postId = editingPostId ?: UUID.randomUUID().toString()

                val postMap = hashMapOf(
                    "id" to postId,
                    "image" to imageName,
                    "username" to username,
                    "imageProfile" to profileImageName,
                    "content" to content,
                    "date" to date,
                    "email" to email
                )

                db.collection("posts").document(postId)
                    .set(postMap)
                    .addOnSuccessListener {
                        val msg = if (editingPostId != null) "Post updated!" else "Post uploaded!"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        resetForm()
                        findNavController().popBackStack() // חזור למסך הקודם
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to upload post: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch user data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetForm() {
        postContentEditText.setText("")
        selectedImageView.setImageDrawable(null)
        selectedImageName = null
        editingPostId = null
        postButton.text = "Post"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                val inputStream: InputStream? = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageView.setImageBitmap(bitmap)
                selectedImageName = "sample_post" // אם מיישמים העלאת קבצים – יש להחליף בשם אמיתי
            }
        }
    }
}

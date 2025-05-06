package com.example.sipscore.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sipscore.R
import com.example.sipscore.models.AccountData
import com.example.sipscore.utils.JsonUtils
import com.example.sipscore.utils.hideKeyboard
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import android.text.InputType
import com.example.sipscore.utils.FirestoreSeeder
import com.google.firebase.auth.FirebaseAuth
import com.example.sipscore.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    companion object {
        private const val DEV_PASSWORD = "dev123"
    }

    private lateinit var profileImage: ImageView
    private lateinit var editProfileTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var logoutButton: ImageButton
    private lateinit var cameraIcon: ImageButton
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveChangesButton: Button
    private lateinit var deleteAccountText: TextView
    private var currentImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2

    private var tapCount = 0
    private var lastTapTime = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profileImage)
        editProfileTitle = view.findViewById(R.id.editProfileTitle)
        backButton = view.findViewById(R.id.backButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        cameraIcon = view.findViewById(R.id.cameraIcon)
        firstNameEditText = view.findViewById(R.id.firstNameEditText)
        lastNameEditText = view.findViewById(R.id.lastNameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        saveChangesButton = view.findViewById(R.id.saveChangesButton)
        deleteAccountText = view.findViewById(R.id.deleteAccountText)

        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPrefs.getString("loggedInEmail", null)
        val users = JsonUtils.loadUserAccounts(requireContext()).toMutableMap()
        val user = users[loggedInEmail]

        editProfileTitle.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastTapTime < 600) {
                tapCount++
                if (tapCount == 3) {
                    showDeveloperAccessDialog()
                    tapCount = 0
                }
            } else {
                tapCount = 1
            }
            lastTapTime = now
        }

        if (user != null) {
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            emailEditText.setText(loggedInEmail)

            user.profileImage?.let {
                val imageFile = File(requireContext().filesDir, it)
                if (imageFile.exists()) {
                    profileImage.setImageURI(Uri.fromFile(imageFile))
                }
            }
        }

        saveChangesButton.setOnClickListener {
            if (loggedInEmail != null) {
                val updatedFirstName = firstNameEditText.text.toString()
                val updatedLastName = lastNameEditText.text.toString()
                val updatedEmail = emailEditText.text.toString()

                if (updatedEmail != loggedInEmail && users.containsKey(updatedEmail)) {
                    Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedUser = user?.copy(
                    firstName = updatedFirstName,
                    lastName = updatedLastName,
                    profileImage = currentImageUri?.lastPathSegment ?: user.profileImage
                )

                if (updatedEmail != loggedInEmail) {
                    users.remove(loggedInEmail)
                }

                if (updatedUser != null) {
                    users[updatedEmail] = updatedUser
                    JsonUtils.saveUserAccounts(requireContext(), users)
                    sharedPrefs.edit().putString("loggedInEmail", updatedEmail).apply()
                    Toast.makeText(requireContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cameraIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        profileImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            sharedPrefs.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        deleteAccountText.setOnClickListener {
            confirmAndDeleteAccount()
        }

        view.setOnTouchListener { v, _ ->
            hideKeyboard(requireActivity())
            v.performClick()
            false
        }
    }

    private fun confirmAndDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                val loggedInEmail = sharedPrefs.getString("loggedInEmail", null)
                if (loggedInEmail != null) {
                    val users = JsonUtils.loadUserAccounts(requireContext()).toMutableMap()
                    users.remove(loggedInEmail)
                    JsonUtils.saveUserAccounts(requireContext(), users)
                    sharedPrefs.edit().remove("loggedInEmail").apply()
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeveloperAccessDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(requireContext())
            .setTitle("Developer Access")
            .setMessage("Enter developer password")
            .setView(input)
            .setPositiveButton("Enter") { _, _ ->
                if (input.text.toString() == DEV_PASSWORD) {
                    FirestoreSeeder.seedCoffeeTypes(requireContext())
                    try {
                        seedPosts(requireContext())
                        FirestoreSeeder.addUsers(requireContext())
                        FirestoreSeeder.addLikes(requireContext())
                        FirestoreSeeder.addComments(requireContext())
                        FirestoreSeeder.addCommentLikes(requireContext())
                    } catch (e: Exception) {
                        Log.e("Seeder", "Developer import failed: ${e.message}")
                    }
                    Toast.makeText(requireContext(), "Database seeded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun seedPosts(context: Context) {
        val posts = listOf(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "image" to "sample_post",
                "username" to "Sip&Score Team",
                "imageProfile" to "default_profile",
                "content" to "Welcome to our cozy coffee community!",
                "date" to "April 27, 2025"
            )
        )

        val db = FirebaseFirestore.getInstance()
        for (post in posts) {
            db.collection("posts").add(post)
        }
    }
}

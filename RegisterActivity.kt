package com.example.sipscore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val firstNameEditText = findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = findViewById<EditText>(R.id.lastNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "username" to "$firstName $lastName",
                        "profileImage" to "profilepic_user1",
                        "ratings" to hashMapOf<String, Double>()
                    )

                    db.collection("users").document(email)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                            // שמירת המשתמש
                            val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                            sharedPrefs.edit().putString("loggedInEmail", email).apply()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Firestore save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

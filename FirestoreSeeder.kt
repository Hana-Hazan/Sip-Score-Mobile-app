package com.example.sipscore.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sipscore.R
import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.sipscore.utils.JsonUtils.loadJsonFromAssets
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.Toast


object FirestoreSeeder {
    private val db = FirebaseFirestore.getInstance()

    fun addUsers(context: Context) {
        val db = FirebaseFirestore.getInstance()
        val jsonStr = loadJsonFromAssets(context, "users.json")
        val jsonArray = JSONArray(jsonStr)

        for (i in 0 until jsonArray.length()) {
            val userObject = jsonArray.getJSONObject(i)
            val email = userObject.keys().next()
            val userData = userObject.getJSONObject(email)

            val userMap = mutableMapOf<String, Any?>()
            userMap["firstName"] = userData.getString("firstName")
            userMap["lastName"] = userData.getString("lastName")
            userMap["username"] = userData.getString("username")
            userMap["password"] = userData.getString("password")
            userMap["profileImage"] = userData.getString("profileImage")

            val ratingsMap = mutableMapOf<String, Double>()
            val ratingsJson = userData.optJSONObject("ratings")
            ratingsJson?.let {
                val keys = it.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    ratingsMap[key] = it.getDouble(key)
                }
            }
            userMap["ratings"] = ratingsMap

            db.collection("users").document(email)
                .set(userMap)
                .addOnSuccessListener {
                    Log.d("Seeder", "Added user: $email")
                }
                .addOnFailureListener {
                    Log.e("Seeder", "Failed to add user $email", it)
                }
        }
    }

    fun addLikes(context: Context) {
        val likesJson = JsonUtils.loadJsonFromAssets(context, "likes.json")
        val likesArray = JSONArray(likesJson)

        for (i in 0 until likesArray.length()) {
            val likeObject = likesArray.getJSONObject(i)

            val postId = likeObject.getString("postId")
            val email = likeObject.getString("email")
            val username = likeObject.getString("username")

            val likeData = hashMapOf(
                "email" to email,
                "username" to username
            )

            db.collection("likes")
                .document("${postId}_$email")
                .set(likeData)
                .addOnSuccessListener { println("Like added: $postId -> $email") }
                .addOnFailureListener { e -> println("Error adding like: ${e.message}") }
        }
    }

    fun addComments(context: Context) {
        try {
            val inputStream = context.assets.open("comments.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val db = Firebase.firestore

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val comment = hashMapOf(
                    "postId" to item.getString("postId"),
                    "email" to item.getString("email"),
                    "username" to item.getString("username"),
                    "content" to item.getString("content"),
                    "date" to item.getString("date")
                )
                db.collection("comments").add(comment)
            }

            Toast.makeText(context, "Comments uploaded successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to upload comments: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun seedCoffeeTypes(context: Context) {
        val jsonStr = context.assets.open("coffeeTypes.json").bufferedReader().use { it.readText() }
        val coffeeArray = JSONArray(jsonStr)
        val db = FirebaseFirestore.getInstance()

        for (i in 0 until coffeeArray.length()) {
            val coffee = coffeeArray.getJSONObject(i)
            val serial = coffee.getString("serial")
            val data = mutableMapOf<String, Any?>()
            for (key in coffee.keys()) {
                data[key] = coffee.get(key)
            }
            db.collection("coffeeTypes")
                .document(serial)
                .set(data)
                .addOnSuccessListener {
                    Log.d("Seeder", "Added ${data["name"]}")
                }
                .addOnFailureListener {
                    Log.e("Seeder", "Failed to add $serial", it)
                }
        }
    }

    fun addCommentLikes(context: Context) {
        try {
            val inputStream = context.assets.open("commentLikes.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val db = Firebase.firestore

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val postId = item.getString("postId")
                val email = item.getString("email")
                val likedBy = mutableListOf<String>()
                val dislikedBy = mutableListOf<String>()

                val likedArray = item.getJSONArray("likedBy")
                for (j in 0 until likedArray.length()) {
                    likedBy.add(likedArray.getString(j))
                }

                val dislikedArray = item.getJSONArray("dislikedBy")
                for (j in 0 until dislikedArray.length()) {
                    dislikedBy.add(dislikedArray.getString(j))
                }

                val docId = "${postId}_${email.replace(".", "_")}"

                val commentLikes = hashMapOf(
                    "postId" to postId,
                    "email" to email,
                    "likedBy" to likedBy,
                    "dislikedBy" to dislikedBy
                )

                db.collection("commentLikes").document(docId).set(commentLikes)
            }

            Toast.makeText(context, "Comment likes uploaded successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to upload comment likes: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}



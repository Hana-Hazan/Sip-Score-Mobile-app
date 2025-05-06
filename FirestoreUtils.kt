package com.example.sipscore.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.*

object FirestoreUtils {
    private val db = FirebaseFirestore.getInstance()

    fun uploadPost(
        content: String,
        imageUrl: String,
        rating: Float,
        authorId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val post = hashMapOf(
            "content" to content,
            "imageUrl" to imageUrl,
            "rating" to rating,
            "authorId" to authorId,
            "timestamp" to Timestamp(Date())
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}

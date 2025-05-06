package com.example.sipscore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import kotlin.random.Random

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dailyQuoteTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        dailyQuoteTextView = findViewById(R.id.dailyQuoteTextView)

        CoroutineScope(Dispatchers.Main).launch {
            val quote = try {
                fetchRandomQuote()
            } catch (e: Exception) {
                "Start your day with a smile ☕️"
            }
            dailyQuoteTextView.text = quote

            delay(5000)

            val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val loggedInEmail = sharedPrefs.getString("loggedInEmail", null)
            val loggedInPassword = sharedPrefs.getString("loggedInPassword", null)

            if (loggedInEmail != null && loggedInPassword != null) {
                auth.signInWithEmailAndPassword(loggedInEmail, loggedInPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        } else {
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        }
                        finish()
                    }
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private suspend fun fetchRandomQuote(): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://zenquotes.io/api/quotes")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonArray = JSONArray(responseBody)
                    val randomIndex = Random.nextInt(jsonArray.length())
                    val randomQuoteObject = jsonArray.getJSONObject(randomIndex)
                    val quoteText = randomQuoteObject.getString("q")
                    quoteText
                } else {
                    "A good coffee makes everything better ☕️"
                }
            }
        }
    }
}

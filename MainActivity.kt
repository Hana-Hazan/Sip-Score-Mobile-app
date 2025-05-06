package com.example.sipscore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.sipscore.fragments.*
import com.example.sipscore.utils.FirestoreSeeder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val postId = intent.getStringExtra("newPostId")
            val fragment = FeedFragment()
            val bundle = Bundle().apply {
                putString("newPostId", postId)
            }
            fragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.nav_home -> FeedFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_post -> NewPostFragment()
                R.id.nav_likes -> NotificationsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, it)
                    .commit()
                true
            } ?: false
        }
    }
}

package com.example.sipscore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.sipscore.R
import com.example.sipscore.models.CoffeeItem
import com.example.sipscore.utils.JsonUtils

class CoffeeDetailsFragment : Fragment() {

    // רכיבי תצוגה של כוסות
    private lateinit var cup1: ImageView
    private lateinit var cup2: ImageView
    private lateinit var cup3: ImageView
    private lateinit var cup4: ImageView
    private lateinit var cup5: ImageView

    // ממוצע וספירת דירוגים
    private lateinit var averageRatingTextView: TextView
    private lateinit var ratingsCountTextView: TextView

    // לב מועדפים
    private lateinit var favoriteIcon: ImageView

    // משתנים כלליים
    private var coffeeItem: CoffeeItem? = null
    private var userEmail: String = "user@example.com" // משתמש מדומה

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coffee_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // קבלת הקפה שהועבר דרך bundle
        coffeeItem = arguments?.get("coffeeItem") as? CoffeeItem

        // אתחול רכיבים
        cup1 = view.findViewById(R.id.cup1)
        cup2 = view.findViewById(R.id.cup2)
        cup3 = view.findViewById(R.id.cup3)
        cup4 = view.findViewById(R.id.cup4)
        cup5 = view.findViewById(R.id.cup5)
        averageRatingTextView = view.findViewById(R.id.averageRatingTextView)
        ratingsCountTextView = view.findViewById(R.id.ratingsCountTextView)
        favoriteIcon = view.findViewById(R.id.favoriteIcon)

        // קבלת אימייל המשתמש המחובר
        val sharedPrefs = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        userEmail = sharedPrefs.getString("loggedInEmail", null) ?: ""

        coffeeItem?.let { coffee ->
            updateRatingDisplay(coffee.averageRating.toFloat(), coffee.ratingCount)
            updateFavoriteIcon(coffee.name)
        }

        setupCupListeners()
        setupFavoriteIconListener()
    }

    private fun updateRatingDisplay(average: Float, count: Int) {
        averageRatingTextView.text = String.format("%.1f", average)
        ratingsCountTextView.text = "$count ratings"
    }

    private fun saveUserRating(rating: Int) {
        coffeeItem?.let { item ->
            val userRatings = JsonUtils.loadUserRatings(requireContext()).toMutableMap()
            val userMap = userRatings.getOrDefault(userEmail, mutableMapOf()).toMutableMap()

            val alreadyRated = userMap.containsKey(item.serial)
            if (!alreadyRated) {
                userMap[item.serial] = rating
                userRatings[userEmail] = userMap

                val newCount = item.ratingCount + 1
                val newSum = (item.averageRating * item.ratingCount) + rating
                val newAverage = (newSum / newCount).toFloat()

                item.ratingCount = newCount
                item.averageRating = newAverage.toDouble()

                updateRatingDisplay(newAverage, newCount)
                JsonUtils.saveUserRatings(requireContext(), userRatings)
            }
        }
    }

    private fun setupCupListeners() {
        val cupViews = listOf(cup1, cup2, cup3, cup4, cup5)
        for (i in cupViews.indices) {
            cupViews[i].setOnClickListener {
                updateCupUI(i + 1)
                saveUserRating(i + 1)
            }
        }
    }

    private fun updateCupUI(rating: Int) {
        val cupViews = listOf(cup1, cup2, cup3, cup4, cup5)
        for (i in cupViews.indices) {
            if (i < rating) {
                cupViews[i].setImageResource(R.drawable.cup_100)
            } else {
                cupViews[i].setImageResource(R.drawable.cup_0)
            }
        }
    }

    private fun updateFavoriteIcon(coffeeName: String) {
        val users = JsonUtils.loadUserAccounts(requireContext()).toMutableMap()
        val user = users[userEmail] ?: return
        val isFavorite = user.favorites.contains(coffeeName)
        favoriteIcon.setImageResource(if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
    }

    private fun setupFavoriteIconListener() {
        favoriteIcon.setOnClickListener {
            coffeeItem?.let { coffee ->
                val users = JsonUtils.loadUserAccounts(requireContext()).toMutableMap()
                val user = users[userEmail] ?: return@let

                val updatedFavorites = user.favorites.toMutableList()
                if (updatedFavorites.contains(coffee.name)) {
                    updatedFavorites.remove(coffee.name)
                    favoriteIcon.setImageResource(R.drawable.ic_heart_outline)
                } else {
                    updatedFavorites.add(coffee.name)
                    favoriteIcon.setImageResource(R.drawable.ic_heart_filled)
                }

                users[userEmail] = user.copy(favorites = updatedFavorites)
                JsonUtils.saveUserAccounts(requireContext(), users)
            }
        }
    }

    companion object {
        fun newInstance(coffeeItem: CoffeeItem): CoffeeDetailsFragment {
            val fragment = CoffeeDetailsFragment()
            val bundle = Bundle().apply {
                putSerializable("coffeeItem", coffeeItem)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
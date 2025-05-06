package com.example.sipscore.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.adapter.CoffeeAdapter
import com.example.sipscore.models.CoffeeItem
import com.example.sipscore.utils.JsonUtils
import com.example.sipscore.fragments.CoffeeDetailsFragment


class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var coffeeAdapter: CoffeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPrefs.getString("loggedInEmail", null) ?: return
        val users = JsonUtils.loadUserAccounts(requireContext())
        val user = users[loggedInEmail] ?: return
        val allCoffees = JsonUtils.loadCoffeeTypes(requireContext())
        val favoriteCoffees = allCoffees.filter { user.favorites.contains(it.name) }

        coffeeAdapter = CoffeeAdapter(favoriteCoffees) { selectedCoffee ->
            val fragment = CoffeeDetailsFragment.newInstance(selectedCoffee)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = coffeeAdapter
    }
}
package com.example.sipscore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.adapter.CoffeeAdapter
import com.example.sipscore.models.loadCoffeeItems
import com.example.sipscore.utils.hideKeyboard

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val coffeeList = loadCoffeeItems(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.coffeeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = CoffeeAdapter(coffeeList) { coffeeItem ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, CoffeeDetailsFragment.newInstance(coffeeItem))
                .addToBackStack(null)
                .commit()
        }

        view.setOnTouchListener { v, _ ->
            hideKeyboard(requireActivity())
            v.performClick()
            false
        }
    }
}

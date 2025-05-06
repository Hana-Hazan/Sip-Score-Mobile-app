package com.example.sipscore.models

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

data class CoffeeItem(
    val name: String,
    val serial: Int,
    val type: String,
    val shopName: String,
    val image: String,
    var averageRating: Double,
    var ratingCount: Int,
    val description: String
):Serializable

fun loadCoffeeItems(context: Context): List<CoffeeItem> {
    val coffeeList = mutableListOf<CoffeeItem>()
    val json = context.assets.open("coffeeTypes.json").bufferedReader().use { it.readText() }
    val jsonArray = JSONArray(json)

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        coffeeList.add(
            CoffeeItem(
                name = obj.getString("name"),
                serial = obj.getInt("serial"),
                type = obj.getString("type"),
                shopName = obj.getString("shopName"),
                image = obj.getString("image"),
                averageRating = obj.getDouble("averageRating"),
                ratingCount = obj.getInt("ratingCount"),
                description=obj.getString("description")
            )
        )
    }
    return coffeeList
}
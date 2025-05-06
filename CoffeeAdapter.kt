package com.example.sipscore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sipscore.R
import com.example.sipscore.models.CoffeeItem

class CoffeeAdapter(
    private val coffeeList: List<CoffeeItem>,
    private val onItemClick: ((CoffeeItem) -> Unit)? = null
) : RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder>() {

    class CoffeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coffeeImage: ImageView = itemView.findViewById(R.id.coffeeImageView)
        val coffeeName: TextView = itemView.findViewById(R.id.coffeeTypeTextView)
        val shopName: TextView = itemView.findViewById(R.id.shopNameTextView)
        val ratingCups: List<ImageView> = listOf(
            itemView.findViewById(R.id.cup1),
            itemView.findViewById(R.id.cup2),
            itemView.findViewById(R.id.cup3),
            itemView.findViewById(R.id.cup4),
            itemView.findViewById(R.id.cup5)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coffee, parent, false)
        return CoffeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        val coffee = coffeeList[position]

        holder.coffeeName.text = coffee.type
        holder.shopName.text = coffee.shopName

        // טעינת תמונה לפי שם קובץ
        val context = holder.itemView.context
        val imageId = context.resources.getIdentifier(
            coffee.image.substringBeforeLast("."), // למקרה שיש סיומת
            "drawable",
            context.packageName
        )
        holder.coffeeImage.setImageResource(imageId)

        // הצגת דירוג בכוסות
        val rating = coffee.averageRating
        for (i in 0 until 5) {
            val cupRes = if (i < rating.toInt()) R.drawable.cup_100 else R.drawable.cup_0
            holder.ratingCups[i].setImageResource(cupRes)
        }

        // מאזין ללחיצה על כל פריט
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(coffee) // קריאה חכמה לפונקציה שהועברה
        }
    }

    override fun getItemCount() = coffeeList.size
}

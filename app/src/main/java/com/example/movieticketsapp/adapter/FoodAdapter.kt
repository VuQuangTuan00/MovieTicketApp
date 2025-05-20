package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.model.Food

class FoodAdapter(
    private val listFood: ArrayList<Food>,
    private val onItemClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val tvName: TextView   = itemView.findViewById(R.id.tvFoodName)
        val tvPrice: TextView  = itemView.findViewById(R.id.tvPrice)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_layout, parent, false)
        return FoodViewHolder(v)
    }

    override fun getItemCount() = listFood.size

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = listFood[position]
        holder.tvName.text   = food.foodName
        holder.tvPrice.text  = "${food.price} đ"
        holder.tvStatus.text = food.status
        Glide.with(holder.itemView)
            .load(food.imgFood)
            .into(holder.imgFood)

        holder.itemView.setOnClickListener { onItemClick(food) }
    }

    fun updateData(newList: List<Food>) {
        listFood.clear()
        listFood.addAll(newList)
        notifyDataSetChanged()
    }
}

package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemFoodAdminBinding
import com.example.movieticketsapp.model.FoodAdmin

class ItemFoodAdminAdapter(
    private val list: List<FoodAdmin>,
    private val onEdit: (FoodAdmin) -> Unit,
    private val onDelete: (FoodAdmin) -> Unit
) : RecyclerView.Adapter<ItemFoodAdminAdapter.FoodVH>() {

    inner class FoodVH(val binding: ItemFoodAdminBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodVH {
        val binding = ItemFoodAdminBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodVH(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: FoodVH, position: Int) {
        val food = list[position]
        with(holder.binding) {
            // load ảnh với placeholder nếu cần
            Glide.with(root)
                .load(food.img_food)
                .centerCrop()
                .into(imgFoodAdmin)

            tvFoodName.text       = food.food_name
            tvDescription.text    = food.description
            tvPriceStatus.text    = "${food.price} đ · ${food.status}"

            btnEdit.setOnClickListener   { onEdit(food) }
            btnDelete.setOnClickListener { onDelete(food) }
        }
    }
}



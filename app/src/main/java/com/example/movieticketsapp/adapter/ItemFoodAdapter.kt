package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemFoodLayoutBinding
import com.example.movieticketsapp.model.Food

class ItemFoodAdapter(
    private val foodList: List<Food>,
    private val onItemClick: (Food) -> Unit
): RecyclerView.Adapter<ItemFoodAdapter.ViewHolder>()  {
    inner class ViewHolder(val binding: ItemFoodLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position < foodList.size) {
                    val food = foodList[position]
                    onItemClick.invoke(food)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFoodLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = foodList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foodList[position]
        holder.binding.tvTitle.text = food.food_name
        holder.binding.tvDescription.text = food.description
        holder.binding.tvPrice.text = "$${food.price}"
        Glide.with(holder.itemView.context)
            .load(food.img_food)
            .into(holder.binding.imgFood)
    }
}
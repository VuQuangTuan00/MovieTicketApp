package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemOrderDetailLayoutBinding
import com.example.movieticketsapp.model.CartItem


class ItemOrderDetailAdapter(
    private val listOrderDetail: MutableList<CartItem>,
    private val onTotal: () -> Unit,
    private val dissmiss: () -> Unit
): RecyclerView.Adapter<ItemOrderDetailAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemOrderDetailLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemOrderDetailLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = listOrderDetail.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listOrderDetail[position]
        val food = item.food

        holder.apply {
            binding.tvFoodName.text = food.food_name
            binding.tvFoodPrice.text = "${food.price}"
            binding.tvFoodDescription.text = food.description
            binding.tvQuantity.text = item.quantity.toString()
            Glide.with(holder.itemView.context)
                .load(food.img_food)
                .into(holder.binding.imgFood)
            binding.btnPlus.setOnClickListener {
                item.quantity++
                binding.tvQuantity.text = item.quantity.toString()
                onTotal()
            }

            binding.btnMinus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    binding.tvQuantity.text = item.quantity.toString()
                    onTotal()
                    onTotal()
                } else {
                    listOrderDetail.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, listOrderDetail.size)
                    onTotal()
                    dissmiss()
                }
            }
        }
    }
}
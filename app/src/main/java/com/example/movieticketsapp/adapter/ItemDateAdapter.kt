package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemDateLayoutBinding

class ItemDateAdapter(private val timeList: List<String>) :
    RecyclerView.Adapter<ItemDateAdapter.ViewHolder>() {
    private var selectedPosition = -1
    private var lastSelectedTime = -1

    inner class ViewHolder(val binding: ItemDateLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            val dateParts = date.split("/")
            if (dateParts.size == 3) {
                binding.tvDay.text = dateParts[0]
                binding.tvMonth.text = "${dateParts[1]} ${dateParts[2]}"

                if (selectedPosition == adapterPosition) {
                    binding.mainLayout.setBackgroundResource(R.drawable.white_bg)
                    binding.tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    binding.tvMonth.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                } else {
                    binding.mainLayout.setBackgroundResource(R.drawable.light_black_bg)
                    binding.tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    binding.tvMonth.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                binding.root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        lastSelectedTime = selectedPosition
                        selectedPosition = position
                        notifyItemChanged(lastSelectedTime)
                        notifyItemChanged(selectedPosition)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemDateAdapter.ViewHolder {
        return ViewHolder(
            ItemDateLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(timeList[position])
    }

    override fun getItemCount(): Int = timeList.size
}
package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemDateLayoutBinding

class ItemDateAdapter(
    private val dateList: List<Pair<String, String>>, // Pair(displayDate, internalDate)
    private val onDateSelected: (String) -> Unit // Passes internalDate
) : RecyclerView.Adapter<ItemDateAdapter.ViewHolder>() {

    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class ViewHolder(val binding: ItemDateLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(displayDate: String, internalDate: String, position: Int) {
            val parts = displayDate.split("/")
            if (parts.size == 3) {
                binding.tvDay.text = parts[1] // Day of month
                binding.tvMonth.text = "${parts[2]}" // Month

                // Update UI for selected state
                if (position == selectedPosition) {
                    binding.mainLayout.setBackgroundResource(R.drawable.white_bg)
                    binding.tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    binding.tvMonth.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                } else {
                    binding.mainLayout.setBackgroundResource(R.drawable.light_black_bg)
                    binding.tvDay.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    binding.tvMonth.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }

                binding.root.setOnClickListener {
                    if (selectedPosition != position) {
                        lastSelectedPosition = selectedPosition
                        selectedPosition = position
                        notifyItemChanged(lastSelectedPosition)
                        notifyItemChanged(selectedPosition)
                        onDateSelected(internalDate)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDateLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (displayDate, internalDate) = dateList[position]
        holder.bind(displayDate, internalDate, position)
    }

    override fun getItemCount(): Int = dateList.size
}

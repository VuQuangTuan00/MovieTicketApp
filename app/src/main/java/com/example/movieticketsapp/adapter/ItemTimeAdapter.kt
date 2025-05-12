package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemTimeLayoutBinding

class ItemTimeAdapter(private val timeList: List<String>) : RecyclerView.Adapter<ItemTimeAdapter.ViewHolder>(){
    private var selectedPosition = -1
    private var lastSelectedTime = -1

    inner class ViewHolder(val binding: ItemTimeLayoutBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(time:String){
            binding.tvTime.text = time
            if (selectedPosition == adapterPosition){
                binding.tvTime.setBackgroundResource(R.drawable.white_bg)
                binding.tvTime.setTextColor(ContextCompat.getColor(itemView.context,R.color.black))
            }else{
                binding.tvTime.setBackgroundResource(R.drawable.light_black_bg)
                binding.tvTime.setTextColor(ContextCompat.getColor(itemView.context,R.color.white))
            }
            binding.root.setOnClickListener{
                val position = adapterPosition
                if (position!= RecyclerView.NO_POSITION){
                    lastSelectedTime = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(lastSelectedTime)
                    notifyItemChanged(selectedPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemTimeAdapter.ViewHolder {
        return ViewHolder(ItemTimeLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ItemTimeAdapter.ViewHolder, position: Int) {
        holder.bind(timeList[position])
    }

    override fun getItemCount(): Int = timeList.size
}
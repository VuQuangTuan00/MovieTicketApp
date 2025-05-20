package com.example.movieticketsapp.adapter

import TicketMovie
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemCastLayoutBinding
import com.example.movieticketsapp.databinding.ItemTicketLayoutBinding
import com.example.movieticketsapp.model.Cast
import com.example.movieticketsapp.model.Movie

class ItemTicketAdapter(private val listCasts: List<TicketMovie>,
                        private var id:String,
                        private val onItemClick: (TicketMovie) -> Unit,
) : RecyclerView.Adapter<ItemTicketAdapter.MyViewHolder>(){
    inner class MyViewHolder(val binding:ItemTicketLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTicketLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = listCasts.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val ticket = listCasts[position]
        holder.binding.tvMovie.text = id
        // 2. Gán click listener
        holder.itemView.setOnClickListener {
            onItemClick(ticket)
        }
    }
}
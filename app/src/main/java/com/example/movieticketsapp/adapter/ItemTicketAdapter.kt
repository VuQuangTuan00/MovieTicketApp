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

class ItemTicketAdapter(
    private val tickets: List<TicketMovie>,
    private val onItemClick: (TicketMovie) -> Unit
) : RecyclerView.Adapter<ItemTicketAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemTicketLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTicketLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = tickets.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.binding.tvMovie.text = ticket.titleMovie
        holder.binding.tvDate.text = ticket.date
        holder.binding.tvHour.text = ticket.hour
        holder.binding.tvSeat.text = ticket.seatIds.joinToString(", ") // nếu seatIds là List<String>
        Glide.with(holder.itemView.context)
            .load(ticket.imgMovie)
            .into(holder.binding.ivPoster)
        holder.itemView.setOnClickListener {
            onItemClick(ticket)
        }
    }
}

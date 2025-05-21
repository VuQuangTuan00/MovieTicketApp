package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.databinding.ItemShowtimeBinding
import com.example.movieticketsapp.model.ShowTime
import java.text.SimpleDateFormat
import java.util.Locale

class ItemShowtimeAdapter(
    private var list: MutableList<ShowTime>,
    private val onEdit: (ShowTime) -> Unit,
    private val onDelete: (ShowTime) -> Unit,
    private val onClick: (ShowTime) -> Unit
) : RecyclerView.Adapter<ItemShowtimeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemShowtimeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShowtimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val showtime = list[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.binding.tvStartTime.text = sdf.format(showtime.start_time)
        holder.binding.tvPrice.text = "Price: ${showtime.price} VNĐ"

        holder.binding.btnEdit.setOnClickListener { onEdit(showtime) }
        holder.binding.btnDelete.setOnClickListener { onDelete(showtime) }

        holder.binding.root.setOnClickListener {
            onClick(showtime)
        }
    }

    fun updateList(newList: List<ShowTime>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
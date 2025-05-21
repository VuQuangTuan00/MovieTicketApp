package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.databinding.ItemTimelineBinding
import com.example.movieticketsapp.model.Timeline
import java.text.SimpleDateFormat
import java.util.Locale

class ItemTimelineAdapter(
    private var list: MutableList<Timeline>,
    private val onEdit: (Timeline) -> Unit,
    private val onDelete: (Timeline) -> Unit,
    private val onClick: (Timeline) -> Unit
) : RecyclerView.Adapter<ItemTimelineAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTimelineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeline = list[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.binding.tvTime.text = sdf.format(timeline.time)

        holder.binding.btnEdit.setOnClickListener { onEdit(timeline) }
        holder.binding.btnDelete.setOnClickListener { onDelete(timeline) }

        holder.binding.root.setOnClickListener {
            onClick(timeline)
        }
    }

    fun updateList(newList: List<Timeline>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}

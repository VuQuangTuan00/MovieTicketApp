package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemCastLayoutBinding
import com.example.movieticketsapp.model.Cast

class ItemCastsAdapter(private val listCasts: List<Cast>) : RecyclerView.Adapter<ItemCastsAdapter.MyViewHolder>(){
    inner class MyViewHolder(val binding:ItemCastLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCastLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = listCasts.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cast = listCasts[position]
        val context = holder.itemView.context
        Glide.with(context)
            .load(cast.avatar)
            .into(holder.binding.imgCast)
        holder.binding.tvNameCast.text = cast.name

    }
}
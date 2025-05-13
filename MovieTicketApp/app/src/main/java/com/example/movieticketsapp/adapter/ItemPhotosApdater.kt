package com.example.movieticketsapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemPhotoLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.example.movieticketsapp.model.Movie


class ItemPhotosApdater(private var listMovie: List<String>): RecyclerView.Adapter<ItemPhotosApdater.MyViewHolder>() {
    inner class MyViewHolder(val binding: ItemPhotoLayoutBinding):RecyclerView.ViewHolder(binding.root)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPhotoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listMovie.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val movie = listMovie[position]
        val context = holder.itemView.context
        Glide.with(context)
            .load(movie)
            .into(holder.binding.imgPhoto)
    }
}
package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemPhotoAdminLayoutBinding

class ImagePhotoAdminAdapter(private val imageList: List<String>) : RecyclerView.Adapter<ImagePhotoAdminAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemPhotoAdminLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemPhotoAdminLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.binding.imgPhoto)
    }

    override fun getItemCount(): Int = imageList.size
}

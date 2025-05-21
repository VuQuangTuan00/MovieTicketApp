package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.databinding.ItemGenreMovieLayoutBinding
import com.example.movieticketsapp.model.GenerMovie

class ItemGenreMovieAdminAdapter(
    private val list: List<GenerMovie>,
    private val onEdit: (GenerMovie) -> Unit,
    private val onDelete: (GenerMovie) -> Unit
) : RecyclerView.Adapter<ItemGenreMovieAdminAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemGenreMovieLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemGenreMovieLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val genre = list[position]
        holder.binding.tvGenerMovie.text = genre.name

        holder.binding.btnEditGener.setOnClickListener { onEdit(genre) }
        holder.binding.btnDeleteGener.setOnClickListener { onDelete(genre) }
    }

    override fun getItemCount(): Int = list.size
}

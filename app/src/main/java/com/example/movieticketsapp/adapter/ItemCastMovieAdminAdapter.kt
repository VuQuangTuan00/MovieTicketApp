package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemCastMovieLayoutBinding
import com.example.movieticketsapp.model.Cast

class ItemCastMovieAdminAdapter (
    private val list: List<Cast>,
    private val onEdit: (Cast) -> Unit,
    private val onDelete: (Cast) -> Unit
) : RecyclerView.Adapter<ItemCastMovieAdminAdapter.CastMovieViewHolder>() {

    inner class CastMovieViewHolder(val binding: ItemCastMovieLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastMovieViewHolder {
        val binding = ItemCastMovieLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastMovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastMovieViewHolder, position: Int) {
        val cast = list[position]
        holder.binding.tvCastName.text = cast.name

        Glide.with(holder.itemView)
            .load(cast.avatar)
            .placeholder(R.drawable.ic_not_image)
            .into(holder.binding.imgCastAvatar)

        holder.binding.btnEditCast.setOnClickListener { onEdit(cast) }
        holder.binding.btnDeleteCast.setOnClickListener { onDelete(cast) }
    }

    override fun getItemCount(): Int = list.size
}
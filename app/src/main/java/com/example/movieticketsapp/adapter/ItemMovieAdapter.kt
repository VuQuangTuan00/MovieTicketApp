package com.example.movieticketsapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemMovieLayoutBinding
import com.example.movieticketsapp.model.Movie


class ItemMovieAdapter(private var listMovie: List<Movie>): RecyclerView.Adapter<ItemMovieAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ItemMovieLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMovieLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {

        return listMovie.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val movie = listMovie[position]
        val context = holder.itemView.context
        Glide.with(context)
            .load(movie.img_movie)
            .into(holder.binding.imgMovie)
        holder.binding.tvTitleMovie.text = movie.title

    }
    fun updateData(newList: List<Movie>) {
        listMovie = newList
        notifyDataSetChanged()
    }
}
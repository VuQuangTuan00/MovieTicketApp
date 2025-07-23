package com.example.movieticketsapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.APIModule.ThemovieAPI.model.GenreModel
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemGenreMovieLayoutBinding


class ItemGenerMovieAdapter(private val list: ArrayList<GenreModel>,val onItemClick: (GenreModel) -> Unit): RecyclerView.Adapter<ItemGenerMovieAdapter.MyViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    init {
        if (list.none { it.name == "All" }) {
            list.add(0, GenreModel(0, "All"))
        }
    }
    inner class MyViewHolder(val binding:ItemGenreMovieLayoutBinding):RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick.invoke(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemGenreMovieLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val generMovie = list[position]
        holder.binding.tvGenerMovie.text = generMovie.name
        if (position == selectedPosition) {
            holder.binding.tvGenerMovie.setBackgroundResource(R.drawable.border_gener_movie_forcus)
        } else {
            holder.binding.tvGenerMovie.setBackgroundResource(R.drawable.border_gener_movie)
        }
    }
}
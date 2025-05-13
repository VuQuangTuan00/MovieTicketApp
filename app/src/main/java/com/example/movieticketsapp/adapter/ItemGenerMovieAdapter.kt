package com.example.movieticketsapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemGenerMovieLayoutBinding
import com.example.movieticketsapp.model.GenerMovie

class ItemGenerMovieAdapter(private val list: List<GenerMovie>,private val onItemClick: (GenerMovie) -> Unit): RecyclerView.Adapter<ItemGenerMovieAdapter.MyViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    inner class MyViewHolder(val binding:ItemGenerMovieLayoutBinding):RecyclerView.ViewHolder(binding.root){
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
        val binding = ItemGenerMovieLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
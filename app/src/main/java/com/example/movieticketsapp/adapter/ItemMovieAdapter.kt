package com.example.movieticketsapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemMovieLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.example.movieticketsapp.model.Movie


class ItemMovieAdapter(private var listMovie: List<Movie>,private val onItemClick: (Movie) -> Unit): RecyclerView.Adapter<ItemMovieAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ItemMovieLayoutBinding):RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
//                val previousPosition = selectedPosition
//                selectedPosition = adapterPosition
//                notifyItemChanged(previousPosition)
//                notifyItemChanged(selectedPosition)
                onItemClick.invoke(listMovie[adapterPosition])
            }
        }
    }

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
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = listMovie.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listMovie[oldItemPosition].title == newList[newItemPosition].title
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listMovie[oldItemPosition] == newList[newItemPosition]
            }
        })
        listMovie = newList
        diffResult.dispatchUpdatesTo(this)
    }
}
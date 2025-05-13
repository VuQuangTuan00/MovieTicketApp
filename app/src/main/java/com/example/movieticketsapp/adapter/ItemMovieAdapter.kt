package com.example.movieticketsapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.activity.ChooseDateAndTimeActivity
import com.example.movieticketsapp.databinding.ItemMovieLayoutBinding
import com.example.movieticketsapp.model.Movie

class ItemMovieAdapter(
    private var listMovie: List<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<ItemMovieAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemMovieLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val movie = listMovie[adapterPosition]
                onItemClick.invoke(movie)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMovieLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = listMovie.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val movie = listMovie[position]
        val context = holder.itemView.context

        holder.binding.tvTitleMovie.text = movie.title

        Glide.with(context)
            .load(movie.img_movie)
            .into(holder.binding.imgMovie)

        holder.binding.btnBookNow.setOnClickListener {
            val intent = Intent(context, ChooseDateAndTimeActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            context.startActivity(intent)
        }
    }

    fun updateData(newList: List<Movie>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = listMovie.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listMovie[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return listMovie[oldItemPosition] == newList[newItemPosition]
            }
        })
        listMovie = newList
        diffResult.dispatchUpdatesTo(this)
    }
}

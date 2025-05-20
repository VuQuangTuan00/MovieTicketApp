package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.databinding.ItemReviewBinding
import com.example.movieticketsapp.model.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val items: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val b = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ReviewViewHolder(private val b: ItemReviewBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(r: Review) {
            b.tvUserName.text     = r.userEmail
            val fiveStars = (r.score / 10.0 * b.ratingBarItem.numStars).toFloat()
            b.ratingBarItem.rating = fiveStars
            b.tvRatingValue.text  = String.format(Locale.getDefault(), "%.0f/10", r.score)
            b.tvComment.text      = r.comment
            b.tvDate.text         = r.date?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
            } ?: ""
        }
    }
}
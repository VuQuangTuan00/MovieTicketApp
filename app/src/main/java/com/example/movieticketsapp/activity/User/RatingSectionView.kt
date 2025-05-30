package com.example.movieticketsapp.activity.User

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.BottomSheetRateBinding
import com.example.movieticketsapp.databinding.BottomSheetThanksBinding
import com.example.movieticketsapp.databinding.ViewRatingSectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class RatingSectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewRatingSectionBinding
    private val db = Firebase.firestore
    private val movieId: String? by lazy {
        (context as? Activity)?.intent?.getStringExtra("movie_id")
    }
    private var selectedStars = 0

    init {
        orientation = VERTICAL
        binding = ViewRatingSectionBinding.inflate(
            LayoutInflater.from(context),this)
        fetchAverageRating()
        binding.btnRateMovie.setOnClickListener { onRateClicked() }
    }

    private fun fetchAverageRating() {
        val id = movieId ?: return
        db.collection("movie")
            .document(id)
            .collection("ratings")
            .get()
            .addOnSuccessListener { snap ->
                val scores = snap.documents.mapNotNull { it.getDouble("score") }
                val avg10 = if (scores.isNotEmpty()) scores.average() else 0.0
                binding.tvAvgRating.text = String.format(
                    Locale.getDefault(),
                    "%.1f",
                    avg10
                )
                val maxStars = binding.ratingBarSmall.numStars
                binding.ratingBarSmall.rating = (avg10 / 5.0 * maxStars).toFloat()
                binding.tvReviewCount.text = "(${scores.size} reviews)"
            }
            .addOnFailureListener {
                // không cần xử lý đặc biệt
            }
    }

    private fun onRateClicked() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để đánh giá.", Toast.LENGTH_SHORT).show()
        } else {
            showRatingDialog()
        }
    }

    private fun showRatingDialog() {
        val dlg = BottomSheetDialog(context)
        val dialogBinding = BottomSheetRateBinding.inflate(LayoutInflater.from(context))
        dlg.setContentView(dialogBinding.root)

        val starViews = listOf(
            dialogBinding.star1, dialogBinding.star2, dialogBinding.star3,
            dialogBinding.star4, dialogBinding.star5
        )

        starViews.forEachIndexed { idx, iv ->
            iv.tag = idx + 1
            iv.setOnClickListener {
                selectedStars = it.tag as Int
                updateStars(starViews, selectedStars)
            }
        }

        dialogBinding.btnCancelRate.setOnClickListener { dlg.dismiss() }
        dialogBinding.btnSubmitRate.setOnClickListener {
            if (selectedStars == 0) {
                Toast.makeText(context, "Vui lòng chọn số sao.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val commentText = dialogBinding.etComment.text
                ?.toString()
                ?.trim()
                .takeIf { !it.isNullOrEmpty() }

            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val email = FirebaseAuth.getInstance().currentUser!!.email.orEmpty()
            val data = hashMapOf<String, Any>(
                "userId"    to uid,
                "userEmail" to email,
                "score"     to selectedStars.toDouble(),
                "createdAt" to FieldValue.serverTimestamp()
            ).apply {
                commentText?.let { this["comment"] = it }
            }

            val ratingsRef = db.collection("movie")
                .document(movieId!!)
                .collection("ratings")

            // Kiểm tra đã có đánh giá chưa
            ratingsRef.whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { query ->
                    val operation = if (query.documents.isNotEmpty()) {
                        // update document đầu tiên
                        query.documents[0].reference.update(data)
                    } else {
                        // add mới
                        ratingsRef.add(data)
                    }

                    operation
                        .addOnSuccessListener {
                            dlg.dismiss()
                            showThanksDialog()
                            fetchAverageRating()
                            // gọi loadReviews() để refresh RecyclerView
                            (context as? DetailsMovieActivity)?.loadReviews()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Gửi thất bại, thử lại sau.", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Lỗi kết nối, thử lại sau.", Toast.LENGTH_SHORT).show()
                }
        }

        dlg.show()
    }

    private fun updateStars(starViews: List<ImageView>, count: Int) {
        starViews.forEachIndexed { idx, iv ->
            iv.setImageResource(
                if (idx < count) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )
        }
    }

    private fun showThanksDialog() {
        val dlg = BottomSheetDialog(context)
        val thanksBinding = BottomSheetThanksBinding.inflate(LayoutInflater.from(context))
        dlg.setContentView(thanksBinding.root)
        dlg.show()
    }
}

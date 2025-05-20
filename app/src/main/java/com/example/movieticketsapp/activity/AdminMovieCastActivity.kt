package com.example.movieticketsapp.activity

import AddEditCastFragment
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemCastMovieAdminAdapter
import com.example.movieticketsapp.databinding.AdminMovieCastLayoutBinding
import com.example.movieticketsapp.model.Cast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AdminMovieCastActivity : AppCompatActivity() {
    private lateinit var binding: AdminMovieCastLayoutBinding
    private val db = FirebaseFirestore.getInstance()
    private val castList = mutableListOf<Cast>()
    private lateinit var adapter: ItemCastMovieAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMovieCastLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Danh sách diễn viên"

        // Đăng ký lắng nghe kết quả từ fragment
        supportFragmentManager.setFragmentResultListener("update_cast_result", this) { _, result ->
            val isUpdated = result.getBoolean("isUpdated", false)
            if (isUpdated) {
                loadCastMovies() // Tải lại danh sách khi có thay đổi
            }
        }

        adapter = ItemCastMovieAdminAdapter(
            castList,
            onEdit = { cast -> openAddEditCastFragment(true, cast.id) },
            onDelete = { cast -> confirmDelete(cast) }
        )

        binding.listCasts.layoutManager = GridLayoutManager(this, 2)
        binding.listCasts.adapter = adapter

        binding.fabAddCast.setOnClickListener {
            openAddEditCastFragment(false, null)
        }

        loadCastMovies()
    }

    private fun loadCastMovies() {
        db.collection("cast")
            .get()
            .addOnSuccessListener { result ->
                castList.clear()
                for (doc in result) {
                    val cast = doc.toObject(Cast::class.java)
                    castList.add(cast)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi tải dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete(cast: Cast) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xoá diễn viên")
            .setMessage("Bạn có chắc chắn muốn xoá ${cast.name}?")
            .setPositiveButton("Xoá") { _, _ ->
                // Xóa ảnh từ Firebase Storage
                val avatarUrl = cast.avatar
                if (avatarUrl.isNotEmpty()) {
                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(avatarUrl)
                    storageReference.delete()
                        .addOnSuccessListener {
                            // Sau khi xóa ảnh, xóa thông tin diễn viên trong Firestore
                            db.collection("cast").document(cast.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Đã xoá ${cast.name}", Toast.LENGTH_SHORT).show()
                                    loadCastMovies() // Tải lại danh sách sau khi xóa
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Lỗi xoá: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Lỗi xóa ảnh: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun openAddEditCastFragment(isEditMode: Boolean, castId: String?) {
        val fragment = AddEditCastFragment()
        val bundle = Bundle()
        bundle.putBoolean("isEditMode", isEditMode)
        castId?.let { bundle.putString("castId", it) }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        if (supportFragmentManager.backStackEntryCount == 0) {
            loadCastMovies() // Tải lại danh sách khi quay lại từ fragment
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

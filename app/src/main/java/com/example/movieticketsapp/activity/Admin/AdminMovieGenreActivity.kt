package com.example.movieticketsapp.activity.Admin

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieticketsapp.adapter.ItemGenreMovieAdminAdapter
import com.example.movieticketsapp.databinding.AdminMovieGenreLayoutBinding
import com.example.movieticketsapp.model.GenerMovie
import com.google.firebase.firestore.FirebaseFirestore

class AdminMovieGenreActivity : AppCompatActivity() {
    private lateinit var binding: AdminMovieGenreLayoutBinding
    private val db = FirebaseFirestore.getInstance()
    private val genreList = mutableListOf<GenerMovie>()
    private lateinit var adapter: ItemGenreMovieAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMovieGenreLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Danh sách thể loại"

        adapter = ItemGenreMovieAdminAdapter(
            genreList,
            onEdit = { genre -> showEditDialog(genre) },
            onDelete = { genre -> confirmDelete(genre) }
        )

        binding.listGeners.layoutManager = GridLayoutManager(this, 2)
        binding.listGeners.adapter = adapter

        binding.fabAddGener.setOnClickListener {
            showAddDialog()
        }

        loadGenres()
    }

    private fun loadGenres() {
        db.collection("gener")
            .orderBy("name")
            .get()
            .addOnSuccessListener { result ->
                genreList.clear()
                for (doc in result) {
                    val genre = doc.toObject(GenerMovie::class.java)
                    genreList.add(genre)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showAddDialog() {
        val input = EditText(this)
        input.hint = "Nhập tên thể loại"

        AlertDialog.Builder(this)
            .setTitle("Thêm thể loại")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val genre = GenerMovie(id = "", name = name)

                    db.collection("gener").add(genre)
                        .addOnSuccessListener { documentReference ->
                            val updatedGenre = genre.copy(id = documentReference.id)
                            db.collection("gener").document(documentReference.id).set(updatedGenre)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Thêm thể loại \"$name\" thành công", Toast.LENGTH_SHORT).show()
                                    loadGenres()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Thêm thể loại thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Thêm thể loại thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditDialog(genre: GenerMovie) {
        val input = EditText(this)
        input.setText(genre.name)

        AlertDialog.Builder(this)
            .setTitle("Sửa thể loại")
            .setView(input)
            .setPositiveButton("Cập nhật") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val updatedGenre = genre.copy(name = newName)

                    db.collection("gener").document(genre.id).set(updatedGenre)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Đã cập nhật thể loại \"${genre.name}\" thành \"$newName\"", Toast.LENGTH_SHORT).show()
                            loadGenres()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Cập nhật thể loại thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun confirmDelete(genre: GenerMovie) {
        AlertDialog.Builder(this)
            .setTitle("Xóa thể loại")
            .setMessage("Bạn có chắc chắn muốn xóa thể loại \"${genre.name}\" không?")
            .setPositiveButton("Xóa") { _, _ ->
                db.collection("gener").document(genre.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Xóa thể loại \"${genre.name}\" thành công", Toast.LENGTH_SHORT).show()
                        loadGenres()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Xóa thể loại thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
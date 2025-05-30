package com.example.movieticketsapp.activity.Admin

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemFoodAdminAdapter
import com.example.movieticketsapp.databinding.AdminFoodManagerLayoutBinding
import com.example.movieticketsapp.model.FoodAdmin
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AdminFoodManagerActivity : AppCompatActivity() {

    private lateinit var binding: AdminFoodManagerLayoutBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val foodList = mutableListOf<FoodAdmin>()
    private lateinit var adapter: ItemFoodAdminAdapter

    // Uri tạm lưu ảnh chọn từ dialog
    private var selectedImageUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminFoodManagerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đăng ký picker
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            selectedImageUri = uri
            // hiển thị ngay preview nếu dialog còn mở:
            currentPreview?.setImageURI(uri)
        }

        // toolbar
        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Danh sách Thực Đơn"

        // adapter + RecyclerView
        adapter = ItemFoodAdminAdapter(
            foodList,
            onEdit   = { food -> showEditDialog(food) },
            onDelete = { food -> confirmDelete(food) }
        )
        binding.listFoods.layoutManager = LinearLayoutManager(this)
        binding.listFoods.adapter = adapter

        // nút thêm
        binding.fabAddFood.setOnClickListener { showAddDialog() }

        loadFoods()
    }

    private fun loadFoods() {
        db.collection("food")
            .orderBy("food_name")
            .get()
            .addOnSuccessListener { result ->
                foodList.clear()
                for (doc in result) {
                    val f = doc.toObject(FoodAdmin::class.java)
                    foodList.add(f.copy(id = doc.id))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải thực đơn: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // giữ tham chiếu tới ImageView preview của dialog hiện tại
    private var currentPreview: ImageView? = null

    private fun showAddDialog() {
        selectedImageUri = null
        val view = layoutInflater.inflate(R.layout.dialog_add_edit_food, null)
        val imgPreview = view.findViewById<ImageView>(R.id.imgFoodPreview)
        currentPreview = imgPreview

        // click để chọn ảnh
        imgPreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val edtName = view.findViewById<EditText>(R.id.edtFoodName)
        val edtDesc = view.findViewById<EditText>(R.id.edtDescription)
        val edtPrice= view.findViewById<EditText>(R.id.edtPrice)
        val edtStatus = view.findViewById<EditText>(R.id.edtStatus)

        AlertDialog.Builder(this)
            .setTitle("Thêm Món Ăn")
            .setView(view)
            .setPositiveButton("Lưu") { _, _ ->
                val name = edtName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                // nếu có chọn ảnh, upload trước
                if (selectedImageUri != null) {
                    val path = "food_images/${UUID.randomUUID()}"
                    storage.child(path).putFile(selectedImageUri!!)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception!!
                            storage.child(path).downloadUrl
                        }
                        .addOnSuccessListener { downloadUri ->
                            saveFoodToFirestore(downloadUri.toString(), name,
                                edtDesc.text.toString(),
                                edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
                                edtStatus.text.toString()
                            )
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Upload ảnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // không chọn ảnh => lưu với img_food=""
                    saveFoodToFirestore("", name,
                        edtDesc.text.toString(),
                        edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
                        edtStatus.text.toString()
                    )
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun showEditDialog(food: FoodAdmin) {
        selectedImageUri = null
        val view = layoutInflater.inflate(R.layout.dialog_add_edit_food, null)
        val imgPreview = view.findViewById<ImageView>(R.id.imgFoodPreview)
        currentPreview = imgPreview

        // load ảnh cũ
        Glide.with(this)
            .load(food.img_food)
            .centerCrop()
            .into(imgPreview)

        imgPreview.setOnClickListener { pickImageLauncher.launch("image/*") }

        val edtName = view.findViewById<EditText>(R.id.edtFoodName)
        val edtDesc = view.findViewById<EditText>(R.id.edtDescription)
        val edtPrice= view.findViewById<EditText>(R.id.edtPrice)
        val edtStatus = view.findViewById<EditText>(R.id.edtStatus)

        edtName.setText(food.food_name)
        edtDesc.setText(food.description)
        edtPrice.setText(food.price.toString())
        edtStatus.setText(food.status)

        AlertDialog.Builder(this)
            .setTitle("Sửa Món Ăn")
            .setView(view)
            .setPositiveButton("Cập nhật") { _, _ ->
                // nếu có ảnh mới thì upload trước, còn không thì giữ URL cũ
                if (selectedImageUri != null) {
                    val path = "food_images/${UUID.randomUUID()}"
                    storage.child(path).putFile(selectedImageUri!!)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception!!
                            storage.child(path).downloadUrl
                        }
                        .addOnSuccessListener { downloadUri ->
                            updateFoodInFirestore(food.id,
                                downloadUri.toString(),
                                edtName.text.toString(),
                                edtDesc.text.toString(),
                                edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
                                edtStatus.text.toString()
                            )
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Upload ảnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    updateFoodInFirestore(food.id,
                        food.img_food,
                        edtName.text.toString(),
                        edtDesc.text.toString(),
                        edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
                        edtStatus.text.toString()
                    )
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun saveFoodToFirestore(
        imgUrl: String,
        name: String,
        desc: String,
        price: Double,
        status: String
    ) {
        val newFood = FoodAdmin(id="", food_name=name,
            description=desc, img_food=imgUrl, price=price, status=status)
        db.collection("food")
            .add(newFood)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã thêm \"$name\"", Toast.LENGTH_SHORT).show()
                loadFoods()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Thêm thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFoodInFirestore(
        id: String,
        imgUrl: String,
        name: String,
        desc: String,
        price: Double,
        status: String
    ) {
        val updated = FoodAdmin(id=id, food_name=name,
            description=desc, img_food=imgUrl, price=price, status=status)
        db.collection("food").document(id)
            .set(updated)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show()
                loadFoods()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Cập nhật thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete(food: FoodAdmin) {
        AlertDialog.Builder(this)
            .setTitle("Xóa Món Ăn")
            .setMessage("Bạn có chắc muốn xóa \"${food.food_name}\" không?")
            .setPositiveButton("Xóa") { _, _ ->
                // xóa doc, không xóa ảnh Storage (nếu muốn bạn có thể thêm)
                db.collection("food").document(food.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
                        loadFoods()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Xóa thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

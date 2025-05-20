package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.movieticketsapp.BottomSheetDialog.FoodBottomSheetFragment
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemFoodAdapter
import com.example.movieticketsapp.databinding.FoodLayoutBinding
import com.example.movieticketsapp.model.Food
import com.google.firebase.firestore.FirebaseFirestore

class FoodActivity : AppCompatActivity() {
    private lateinit var binding: FoodLayoutBinding
    private lateinit var adapter: ItemFoodAdapter
    private lateinit var listFood: ArrayList<Food>
    private lateinit var imgList: ArrayList<SlideModel>
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(binding.root)
        fetchFoodData()
        fetchListImgFood()
        fetchLocation()
    }

    private fun initialize() {
        db = FirebaseFirestore.getInstance()
        imgList = ArrayList()
        binding = FoodLayoutBinding.inflate(layoutInflater)
        listFood = ArrayList()
    }

    private fun setAdapter(list: ArrayList<Food>) {
        val layoutManager = GridLayoutManager(this, 2)
        binding.rcvFood.layoutManager = layoutManager
        adapter = ItemFoodAdapter(list) { selectedFood ->
            val bottomSheet = FoodBottomSheetFragment(selectedFood)
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
        binding.rcvFood.adapter = adapter
    }

    private fun fetchFoodData() {
        db.collection("food")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching food", e)
                    return@addSnapshotListener
                }
                listFood.clear()
                for (doc in snapshot!!) {
                    val food = doc.toObject(Food::class.java)
                    listFood.add(food)
                }

                setAdapter(listFood)
                adapter.notifyDataSetChanged()
            }
    }
    private fun fetchListImgFood() {
        db.collection("list_img_food")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching image list", e)
                    return@addSnapshotListener
                }

                imgList.clear()
                for (doc in snapshot!!) {
                    val img = doc.getString("img")
                    if (!img.isNullOrEmpty()) {
                        imgList.add(SlideModel(img, ScaleTypes.FIT))
                    }
                }

                binding.imgSlider.setImageList(imgList, ScaleTypes.FIT)
            }
    }
    private fun fetchLocation() {
        var address = ""
        db.collection("cinema")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching image list", e)
                    return@addSnapshotListener
                }

                imgList.clear()
                for (doc in snapshot!!) {
                    address = doc.getString("address").orEmpty()
                }
                binding.edtReceived.setText(address)
            }
    }
}
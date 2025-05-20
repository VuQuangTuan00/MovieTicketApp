package com.example.movieticketsapp.activity

import android.os.Bundle
import android.util.Log
import android.view.View
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
    private val cartItems = mutableListOf<Pair<Food, Int>>()
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
            val bottomSheet = FoodBottomSheetFragment(
                selectedFood,
                onAddToBasket = {quantity->
                    updateCart(selectedFood, quantity)
                    Log.d("AddToBasket", "Item added to basket: ${binding.lnCartSummaryBar.visibility}")
                }
            )
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
                snapshot?.forEach { doc ->
                    val food = doc.toObject(Food::class.java)
                    food.food_id = doc.id
                    listFood.add(food)
                }

                setAdapter(listFood)
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
                snapshot?.forEach { doc ->
                    doc.getString("img")?.let { img ->
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
                for (doc in snapshot!!) {
                    address = doc.getString("address").orEmpty()
                }
                binding.edtReceived.setText(address)
            }
    }
    private fun updateCart(food: Food, quantity: Int) {

        val existingIndex = cartItems.indexOfFirst { it.first.food_id == food.food_id }
        if (existingIndex != -1) {
            val existingItem = cartItems[existingIndex]
            cartItems[existingIndex] = existingItem.copy(second = existingItem.second + quantity)
        } else {
            cartItems.add(Pair(food, quantity))
        }


        val totalQuantity = cartItems.sumOf { it.second }
        val totalPrice = cartItems.sumOf { it.first.price * it.second }

        binding.lnCartSummaryBar.visibility = View.VISIBLE
        binding.tvCartItems.text = "$totalQuantity item"
        binding.tvCartTotalPrice.text = "$${String.format("%.2f", totalPrice)}"
    }
}
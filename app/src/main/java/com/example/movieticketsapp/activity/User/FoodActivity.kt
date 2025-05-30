package com.example.movieticketsapp.activity.User

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.movieticketsapp.BottomSheetDialog.FoodBottomSheetFragment
import com.example.movieticketsapp.adapter.ItemFoodAdapter
import com.example.movieticketsapp.databinding.FoodLayoutBinding
import com.example.movieticketsapp.model.CartItem
import com.example.movieticketsapp.model.Food
import com.google.firebase.firestore.FirebaseFirestore

class FoodActivity : AppCompatActivity() {
    private lateinit var binding: FoodLayoutBinding
    private lateinit var adapter: ItemFoodAdapter
    private lateinit var listFood: ArrayList<Food>
    private lateinit var imgList: ArrayList<SlideModel>
    private lateinit var db: FirebaseFirestore
    private  var receivedAt: String = ""
    private  var foodDeliveryDate: String = ""
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContentView(binding.root)
        fetchFoodData()
        fetchListImgFood()
        fetchLocation()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        cartItems.clear()
        binding.lnCartSummaryBar.visibility = View.GONE
        Log.d("FFFF","${cartItems.size}")
    }
    private fun setEvent() {
        binding.lnCartSummaryBar.setOnClickListener {
            foodDeliveryDate = binding.edtFoodDeliveryDate.text.toString()
            val intent = Intent(this, BillDetailsActivity::class.java)
            val cartMutableList: MutableList<CartItem> = cartItems.toMutableList()
            intent.putParcelableArrayListExtra("cartItems", ArrayList(cartMutableList))
            intent.putExtra("receivedAt", receivedAt)
            intent.putExtra("foodDeliveryDate", foodDeliveryDate)
            Log.d("receivedAt", receivedAt)
            Log.d("foodDeliveryDate", foodDeliveryDate)
            startActivity(intent)
        }
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
                onAddToBasket = { quantity ->
                    updateCart(selectedFood, quantity)
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
                    Log.d("ListFood", "List of food: $listFood")
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
                    address = doc.getString("cinema_name").orEmpty()
                }
                binding.edtReceived.setText(address)
                receivedAt = address
            }
    }

    private fun updateCart(food: Food, quantity: Int) {
        val existingItem = cartItems.find { it.food.food_id == food.food_id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(food, quantity))
        }

        val totalQuantity = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.food.price * it.quantity }

        binding.lnCartSummaryBar.visibility = View.VISIBLE
        binding.tvCartItems.text = "$totalQuantity item${if (totalQuantity > 1) "s" else ""}"
//        binding.tvCartTotalPrice.text = "$${"%.2f".format(totalPrice)}"
        binding.tvCartTotalPrice.text = "$$totalPrice"
    }
}
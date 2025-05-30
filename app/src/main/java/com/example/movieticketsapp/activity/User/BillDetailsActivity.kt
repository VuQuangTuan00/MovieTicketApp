package com.example.movieticketsapp.activity.User

import android.app.ComponentCaller
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.ZaloPay.ZaloPayHelper
import com.example.movieticketsapp.adapter.ItemNoteAdapter
import com.example.movieticketsapp.adapter.ItemOrderDetailAdapter
import com.example.movieticketsapp.databinding.BillDetailsLayoutBinding
import com.example.movieticketsapp.model.CartItem
import com.example.movieticketsapp.model.Items
import com.example.movieticketsapp.model.Orders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK

class BillDetailsActivity : AppCompatActivity() {
    private lateinit var binding: BillDetailsLayoutBinding
    private lateinit var cartItems: MutableList<CartItem>
    private lateinit var foodDeliveryDate: String
    private lateinit var receivedAt: String
    private  var listNote: ArrayList<String> = ArrayList()
    private lateinit var dialog: Dialog
    private  var totalPrice:Double = 0.0
    private  var userId :String = ""
    private lateinit var db: FirebaseFirestore
    private var policy: StrictMode.ThreadPolicy? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BillDetailsLayoutBinding.inflate(layoutInflater)
        db = FirebaseFirestore.getInstance()
        dialog = Dialog(this)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        setContentView(binding.root)
        cartItems = intent.getParcelableArrayListExtra<CartItem>("cartItems")?.toMutableList() ?: mutableListOf()
        foodDeliveryDate = intent.getStringExtra("foodDeliveryDate") ?: ""
        receivedAt = intent.getStringExtra("receivedAt") ?: ""
        policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        ZaloPaySDK.init(2553, Environment.SANDBOX)
        setAdapter()
        fetchUser()
        fetchNote()
        setEvents()
    }

    private fun setEvents() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnPay.setOnClickListener {
            val total = totalPrice.toInt() * 25000
            ZaloPayHelper(this@BillDetailsActivity).pay(
                priceText = total.toString()
                , onSuccess = {
                    uploadOrderToFirebase(cartItems, receivedAt, foodDeliveryDate)
                },
                onFailure = { e ->
                    Log.e("ZALO_PAY", "Lỗi thanh toán: ${e?.message}")
                }
            )
        }
        binding.tvCinema.text = receivedAt
        binding.tvReceived.text = foodDeliveryDate
        binding.lnInfo.setOnLongClickListener {
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun setAdapter() {
        updateSummary()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvOrderDetail.layoutManager = layoutManager
        val adapter = ItemOrderDetailAdapter(cartItems, onTotal = {
            updateSummary()
        },
            dissmiss = {
                finish()
            }
            )
        binding.rcvOrderDetail.adapter = adapter
    }
    private fun fetchNote() {
        db.collection("note")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("fetchNote", "Error fetching note", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (doc in snapshot) {
                        val note = doc.getString("content")
                        if (note != null) {
                            listNote.add(note)
                        }
                    }
                    val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    binding.rcvNote.layoutManager = layoutManager
                    val adapterOD = ItemNoteAdapter(listNote)
                    binding.rcvNote.adapter = adapterOD
                }
            }
    }

    private fun updateSummary() {
         totalPrice = cartItems.sumOf { it.food.price * it.quantity }
        binding.tvTotal.text = "$${totalPrice.toInt()}"
    }
    private fun fetchUser() {
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("fetchUser", "Error fetching user", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val name = snapshot.getString("name").orEmpty()
                    val phone = snapshot.getString("phone").orEmpty()
                    val email = snapshot.getString("email").orEmpty()

                    binding.tvUserName.text = name
                    binding.tvPhone.text = phone
                    binding.tvEmail.text = email
                } else {
                    Log.d("fetchUser", "No such document")
                }
            }
    }
    private fun uploadOrderToFirebase(
        cartItems: List<CartItem>,
        receivedAt: String,
        foodDeliveryDate: String
    ) {
        val db = FirebaseFirestore.getInstance()


        val orderId = db.collection("users")
            .document(userId).collection("orders").document().id

        val orderData = Orders(
            receivedAt = receivedAt,
            foodDeliveryDate = foodDeliveryDate,
            status = "pending",
            timestamp = FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("orders")
            .document(orderId)
            .set(orderData)
            .addOnSuccessListener {
                Log.d("Firestore", "Order info uploaded")

                val itemsCollection = db.collection("users")
                    .document(userId).collection("orders").document(orderId).collection("items")

                val batch = db.batch()

                for (item in cartItems) {
                    val itemRef = itemsCollection.document()
                    val itemData = Items(
                        food_id = item.food.food_id,
                        food_name = item.food.food_name,
                        price = item.food.price,
                        quantity = item.quantity,
                        total =  totalPrice
                    )
                    batch.set(itemRef, itemData)
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("Firestore", "Cart items uploaded")
                        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error uploading cart items", e)
                    }

            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error uploading order", e)
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}

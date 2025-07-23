package com.example.movieticketsapp.CustomView.Dialog.BottomSheetDialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.movieticketsapp.R
import com.example.movieticketsapp.model.Food
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.internal.notifyAll

class FoodBottomSheetFragment(
    private val food: Food,
    private val onAddToBasket: ((quantity: Int) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private lateinit var tvQuantity: TextView
    private lateinit var btnAdd: Button
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.food_bottm_sheet_layout, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvQuantity = view.findViewById(R.id.tvQuantity)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        btnAdd = view.findViewById(R.id.btnAddToBasket)
        val tvFoodName = view.findViewById<TextView>(R.id.tvFoodName)
        val tvFoodPrice = view.findViewById<TextView>(R.id.tvFoodPrice)
        val btnCancel = view.findViewById<ImageView>(R.id.btnCancel)
        val tvFoodDescription = view.findViewById<TextView>(R.id.tvFoodDescription)
        val imgFood = view.findViewById<ImageView>(R.id.imgFood)

        tvFoodName.text = food.food_name
        tvFoodPrice.text = "$${food.price}"
        tvFoodDescription.text = food.description
        Glide.with(requireContext()).load(food.img_food).into(imgFood)

        updateButtonText()

        btnPlus.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
            updateButtonText()
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
                updateButtonText()
            } else {
                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Added $quantity items", Toast.LENGTH_SHORT).show()
            onAddToBasket?.invoke(quantity)
            dismiss()
        }
    }

    private fun updateButtonText() {
        val totalPrice = food.price * quantity
        btnAdd.text = "Add to Basket - $${String.format("%.2f", totalPrice)}"
    }
}


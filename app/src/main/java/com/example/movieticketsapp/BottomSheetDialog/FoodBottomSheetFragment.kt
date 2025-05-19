package com.example.movieticketsapp.BottomSheetDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.movieticketsapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FoodBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var tvQuantity: TextView
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.food_bottm_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvQuantity = view.findViewById(R.id.tvQuantity)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        val btnAdd = view.findViewById<Button>(R.id.btnAddToBasket)

        btnPlus.setOnClickListener {
                quantity++
                tvQuantity.text = quantity.toString()
                if (quantity == 1){
                    btnAdd.isEnabled = true
                }
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
            else {
                btnAdd.isEnabled = true
            }
        }

        btnAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Added $quantity items", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}

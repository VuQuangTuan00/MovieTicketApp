package com.example.movieticketsapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.activity.AdminHomeActivity
import com.example.movieticketsapp.databinding.ItemMenuBinding
import com.example.movieticketsapp.model.MenuItem

class MenuAdapter(private val menuItems: List<MenuItem>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.binding.tvMenuItem.text = menuItem.name
        holder.binding.imgMenuItem.setImageResource(menuItem.iconRes)

        holder.itemView.setOnClickListener {
            if (menuItem.name == "Admin Panel") {
                val context = holder.itemView.context
                val intent = Intent(context, AdminHomeActivity::class.java)
                context.startActivity(intent)
            } else {
                // Handle clicks for other menu items
            }
        }
    }

    override fun getItemCount(): Int = menuItems.size
}

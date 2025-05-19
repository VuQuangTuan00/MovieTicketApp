package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemSeatLayoutBinding
import com.example.movieticketsapp.model.Seat

class ItemSeatAdapter(
    seatList: List<Seat>,
    private val selected: SelectedSeat
) : RecyclerView.Adapter<ItemSeatAdapter.ViewHolder>() {

    private val seatData = seatList.toMutableList()
    private val selectedSeatNames = arrayListOf<String>()

    inner class ViewHolder(val binding: ItemSeatLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSeatLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = seatData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seat = seatData[position]
        holder.binding.seat.text = seat.code

        when (seat.status) {
            "AVAILABLE" -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_available)
                holder.binding.seat.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
            "SELECTED" -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_selected)
                holder.binding.seat.setTextColor(holder.itemView.context.getColor(R.color.black))
            }
            "UNAVAILABLE" -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_unavailable)
                holder.binding.seat.setTextColor(holder.itemView.context.getColor(R.color.primaryEnableGray))
            }
        }

        holder.binding.seat.setOnClickListener {
            when (seat.status) {
                "AVAILABLE" -> {
                    seat.status = "SELECTED"
                    selectedSeatNames.add(seat.code)
                    notifyItemChanged(position)
                }
                "SELECTED" -> {
                    seat.status = "AVAILABLE"
                    selectedSeatNames.remove(seat.code)
                    notifyItemChanged(position)
                }
                else -> {}
            }
            selected.onSelectedSeat(ArrayList(selectedSeatNames), selectedSeatNames.size)
        }
    }

    interface SelectedSeat {
        fun onSelectedSeat(selectedNames: ArrayList<String>, num: Int)
    }
}

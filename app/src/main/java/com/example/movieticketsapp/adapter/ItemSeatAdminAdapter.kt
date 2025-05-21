package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.model.Seat

class ItemSeatAdminAdapter(
    private val seats: List<Seat>,
    private val onSeatClick: (Seat) -> Unit
) : RecyclerView.Adapter<ItemSeatAdminAdapter.SeatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seat_admin, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seats[position]
        holder.bind(seat)
        holder.itemView.setOnClickListener {
            onSeatClick(seat)
        }
    }

    override fun getItemCount(): Int = seats.size

    class SeatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSeatCode: TextView = itemView.findViewById(R.id.tvSeatCode)

        fun bind(seat: Seat) {
            tvSeatCode.text = seat.seatCode

            val backgroundRes = when (seat.status) {
                Seat.Status.AVAILABLE -> R.drawable.ic_seat_available
                Seat.Status.SELECTED -> R.drawable.ic_seat_selected
                Seat.Status.UNAVAILABLE -> R.drawable.ic_seat_unavailable
            }
            tvSeatCode.setBackgroundResource(backgroundRes)
        }
    }
}

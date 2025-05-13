package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.ItemSeatLayoutBinding
import com.example.movieticketsapp.model.Seat

class ItemSeatAdapter(
    private val seatList: List<Seat>,
    private val selected:SelectedSeat
    ) : RecyclerView.Adapter<ItemSeatAdapter.ViewHolder>(){
        private var selectedSeatName = mutableListOf<String>()


    inner class ViewHolder(val binding: ItemSeatLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSeatLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {return seatList.size}

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val seat = seatList[position]
        holder.binding.seat.text = seat.seatCode
        when (seat.status) {
            Seat.Status.AVAILABLE -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_available)
                holder.binding.seat.setTextColor(holder.itemView.context.getColor(R.color.white))
            }
            Seat.Status.SELECTED -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_selected)
                holder.binding.seat.setTextColor(holder.itemView.context.getColor(R.color.black))
            }
            Seat.Status.UNAVAILABLE -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_unavailable)
                holder.binding.seat.setTextColor(holder.itemView.context.getColor(R.color.primaryEnableGray))
            }
        }
        holder.binding.seat.setOnClickListener {
            when (seat.status) {
                Seat.Status.AVAILABLE -> {
                    seat.status = Seat.Status.SELECTED
                    selectedSeatName.add(seat.seatCode)
                    notifyItemChanged(position)
                }
                Seat.Status.SELECTED -> {
                    seat.status = Seat.Status.AVAILABLE
                    selectedSeatName.remove(seat.seatCode)
                    notifyItemChanged(position)
                }
               else -> {}
            }
            val selectedName = selectedSeatName.joinToString(", ")
            selected.onSelectedSeat(selectedName,selectedSeatName.size)
        }
    }
    interface SelectedSeat{
        fun onSelectedSeat(selectedNme:String,num: Int)
    }
}
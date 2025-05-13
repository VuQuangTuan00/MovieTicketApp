package com.example.movieticketsapp.model

data class Seat(val id:String, val seatCode:String, val row:String, val col:String, var status:Status){

    enum class Status{
        AVAILABLE,
        SELECTED,
        UNAVAILABLE
    }
}

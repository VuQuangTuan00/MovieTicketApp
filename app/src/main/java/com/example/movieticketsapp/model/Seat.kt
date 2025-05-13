package com.example.movieticketsapp.model

data class Seat(val code:String,val row:String, val column:Int, var status:String){

    enum class Status{
        AVAILABLE,
        SELECTED,
        UNAVAILABLE
    }
}
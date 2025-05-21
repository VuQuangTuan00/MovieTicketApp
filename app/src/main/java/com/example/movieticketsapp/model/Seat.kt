package com.example.movieticketsapp.model

data class Seat(
    val id: String = "",
    val seatCode: String = "",
    val row: String = "",
    val col: Int = 0,
    var status: String = ""
) {
    enum class Status {
        AVAILABLE,
        SELECTED,
        UNAVAILABLE
    }
}

package com.example.movieticketsapp.model

data class Cast (
    var id: String = "",
    var avatar: String = "",
    var name: String = ""
) {
    constructor() : this("", "", "")
}
data class TicketMovie(
    var id: String = "",
    var movieId: String = "",
    var titleMovie: String = "",
    var imgMovie: String = "",
    var seatIds: ArrayList<String> = ArrayList(),
    var totalAmounts: Double = 0.0,
    var date: String = "",
    var hour: String = "",
    var cinemaId: String = "",
    var showTimeId: String = "",
    var timelineId: String = "",
    var standard: Double = 0.0,
    val conversionFee: Double = 0.0,
    val status: String = "pending"
)

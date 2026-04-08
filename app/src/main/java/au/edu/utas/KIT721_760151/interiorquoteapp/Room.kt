package au.edu.utas.KIT721_760151.interiorquoteapp

data class Room(
    var id: String = "",
    var name: String = "",
    var labourCost: Double = 0.0,
    var houseId: String = "",
    var includedInQuote: Boolean = true
)
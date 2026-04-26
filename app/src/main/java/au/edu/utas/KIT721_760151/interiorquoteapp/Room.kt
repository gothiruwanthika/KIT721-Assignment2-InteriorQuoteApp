package au.edu.utas.KIT721_760151.interiorquoteapp

data class Room(
    var id: String = "",
    var name: String = "",
    var labourCost: Double = 0.0,
    var includedInQuote: Boolean = true,
    var imageBase64: String = "",
    var windowCount: Int = 0,
    var floorCount: Int = 0
)
package au.edu.utas.KIT721_760151.interiorquoteapp

data class FloorSpace(
    var id: String = "",
    var name: String = "",
    var width: Int = 0,
    var length: Int = 0,
    var houseId: String = "",
    var roomId: String = "",
    var productId: String = "",
    var productName: String = "",
    var productPricePerSqm: Double = 0.0,
    var variantName: String = "Default"
)
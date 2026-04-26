package au.edu.utas.KIT721_760151.interiorquoteapp

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val pricePerSqm: Double = 0.0,
    val imageUrl: String = "",
    val colourVariants: List<String> = emptyList(),
    val minWidth: Int? = null,
    val maxWidth: Int? = null,
    val minHeight: Int? = null,
    val maxHeight: Int? = null,
    val maxPanels: Int? = null
)
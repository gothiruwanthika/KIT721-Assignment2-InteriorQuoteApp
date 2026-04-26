package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivitySelectProductBinding
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONObject

class SelectProductActivity : AppCompatActivity() {

    private lateinit var ui: ActivitySelectProductBinding
    private var selectionType: String = "window"
    private var houseId: String = ""
    private var roomId: String = ""

    private var currentWindowWidth: Int = 0
    private var currentWindowHeight: Int = 0
    private var currentSelectedProductId: String = ""

    private var windowList = mutableListOf<Window>()
    private var floorSpaceList = mutableListOf<FloorSpace>()
    private var selectedItemIndex = 0

    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    private var recommendedProduct: Product? = null
    private var selectedDifferentProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(ui.root)

        selectionType = intent.getStringExtra("selectionType")?.trim()?.lowercase() ?: "window"
        houseId = intent.getStringExtra("houseId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""

        currentWindowWidth = intent.getIntExtra("currentWindowWidth", 0)
        currentWindowHeight = intent.getIntExtra("currentWindowHeight", 0)
        currentSelectedProductId = intent.getStringExtra("currentSelectedProductId") ?: ""

        ui.btnBack.setOnClickListener {
            finish()
        }

        setupProductRecycler()

        ui.rbUseRecommended.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedDifferentProduct = null
                productAdapter.setSelectedProduct(null)

                val recommended = recommendedProduct
                if (recommended != null) {
                    ui.tvRecommendedSummary.text =
                        "${recommended.name}, $${recommended.pricePerSqm} / m²"
                } else {
                    ui.tvRecommendedSummary.text = "No compatible recommended product"
                }

                updateSelectionModeUI()
            }
        }

        ui.rbChooseDifferent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateSelectionModeUI()
            }
        }

        ui.btnSaveProduct.setOnClickListener {
            val productToReturn = if (ui.rbUseRecommended.isChecked) {
                recommendedProduct
            } else {
                selectedDifferentProduct
            }

            if (productToReturn == null) {
                Toast.makeText(this, "Please select a compatible product", Toast.LENGTH_SHORT).show()
            } else {
                returnSelectedProduct(productToReturn, ui.switchApplySameSelection.isChecked)
            }
        }

        ui.btnApplyToAllWindows.setOnClickListener {
            ui.switchApplySameSelection.isChecked = true
            Toast.makeText(this, "Apply to all enabled", Toast.LENGTH_SHORT).show()
        }

        loadRoomItems()
        updateSelectionModeUI()
        fetchProductsFromApi()
    }

    private fun setupProductRecycler() {
        productAdapter = ProductAdapter(productList) { product ->
            if (ui.rbUseRecommended.isChecked) {
                Toast.makeText(
                    this,
                    "Choose 'Different Product' first to select another product",
                    Toast.LENGTH_SHORT
                ).show()
                return@ProductAdapter
            }

            if (selectionType == "window") {
                val reason = getWindowConstraintFailureReason(
                    product = product,
                    width = currentWindowWidth,
                    height = currentWindowHeight
                )

                if (reason != null) {
                    Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
                    return@ProductAdapter
                }
            }

            selectedDifferentProduct = product
            productAdapter.setSelectedProduct(product.id)
            ui.tvRecommendedSummary.text = "${product.name}, $${product.pricePerSqm} / m²"
        }

        ui.recyclerProducts.layoutManager = LinearLayoutManager(this)
        ui.recyclerProducts.adapter = productAdapter
        ui.recyclerProducts.visibility = View.VISIBLE
    }

    private fun fetchProductsFromApi() {
        val category = if (selectionType == "floor") "floor" else "window"
        val url = "https://utasbot.dev/kit305_2026/product?category=$category"

        val requestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                productList.clear()

                val dataArray = response.optJSONArray("data")
                if (dataArray != null) {
                    for (i in 0 until dataArray.length()) {
                        val productJson = dataArray.getJSONObject(i)
                        val product = parseProduct(productJson)

                        if (product.category.lowercase() == category) {
                            productList.add(product)
                        }
                    }
                }

                productAdapter.notifyDataSetChanged()
                ui.recyclerProducts.visibility = View.VISIBLE

                if (selectionType == "window") {
                    recommendedProduct = productList.firstOrNull {
                        getWindowConstraintFailureReason(
                            product = it,
                            width = currentWindowWidth,
                            height = currentWindowHeight
                        ) == null
                    }
                } else {
                    recommendedProduct = productList.firstOrNull()
                }

                if (recommendedProduct != null) {
                    updateRecommendedProductUI()
                } else {
                    ui.tvRecommendedProductName.text = "No compatible product"
                    ui.tvRecommendedProductPrice.text = "No recommended product fits this window"
                    ui.tvRecommendedSummary.text = "No compatible recommended product"
                    ui.imgRecommendedProduct.setImageResource(android.R.drawable.ic_menu_info_details)
                }

                if (currentSelectedProductId.isNotBlank()) {
                    val existingSelected = productList.firstOrNull { it.id == currentSelectedProductId }
                    if (existingSelected != null) {
                        selectedDifferentProduct = existingSelected
                        productAdapter.setSelectedProduct(existingSelected.id)
                    }
                }

                if (productList.isEmpty()) {
                    Toast.makeText(this, "No $category products found", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(
                    this,
                    "Error loading $category products",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        requestQueue.add(request)
    }

    private fun parseProduct(productJson: JSONObject): Product {
        return Product(
            id = productJson.optString("id", ""),
            name = productJson.optString("name", ""),
            description = productJson.optString("description", ""),
            category = productJson.optString("category", ""),
            pricePerSqm = productJson.optDouble("price_per_sqm", 0.0),
            imageUrl = productJson.optString("imageUrl", ""),
            colourVariants = parseStringList(productJson.optJSONArray("variants")),
            minWidth = if (productJson.isNull("min_width")) null else productJson.optInt("min_width"),
            maxWidth = if (productJson.isNull("max_width")) null else productJson.optInt("max_width"),
            minHeight = if (productJson.isNull("min_height")) null else productJson.optInt("min_height"),
            maxHeight = if (productJson.isNull("max_height")) null else productJson.optInt("max_height"),
            maxPanels = if (productJson.isNull("max_panels")) null else productJson.optInt("max_panels")
        )
    }

    private fun parseStringList(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()

        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.optString(i))
        }
        return list
    }

    private fun loadRoomItems() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .get()
            .addOnSuccessListener { roomDoc ->
                val roomName = roomDoc.getString("name") ?: "Room"
                ui.tvRoomInfo.text = "Room : $roomName"

                if (selectionType == "floor") {
                    ui.tvWindowsInRoom.text = "Floor in this room:"
                    ui.rbChooseDifferent.text = "Choose Different Product for this Floor Space"
                    ui.tvApplySameSelectionLabel.text = "Apply same selection to all floor spaces in this room"
                    ui.tvApplySameSelectionHint.text =
                        "This will update the other floor spaces in this room with the same product"
                    ui.tvRecommendedNote.text = "Suggested for all floor spaces in this room"
                    ui.btnApplyToAllWindows.text = "Apply to All Floor Spaces"
                    loadFloorSpaces()
                } else {
                    ui.tvWindowsInRoom.text = "Windows in this room:"
                    ui.rbChooseDifferent.text = "Choose Different Product for this Window"
                    ui.tvApplySameSelectionLabel.text = "Apply same selection to all windows in this room"
                    ui.tvApplySameSelectionHint.text =
                        "This will update the other windows in this room with the same product"
                    ui.tvRecommendedNote.text = "Suggested for all windows in this room"
                    ui.btnApplyToAllWindows.text = "Apply to All Windows"
                    loadWindows()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading room: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateRecommendedProductUI() {
        val product = recommendedProduct ?: return

        ui.tvRecommendedProductName.text = product.name
        ui.tvRecommendedProductPrice.text = "$${product.pricePerSqm} / m² · ${product.description}"
        ui.tvRecommendedSummary.text = "${product.name}, $${product.pricePerSqm} / m²"
        ui.imgRecommendedProduct.setImageResource(getProductImage(product.name))
    }

    private fun loadWindows() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("windows")
            .get()
            .addOnSuccessListener { documents ->
                windowList.clear()

                for (document in documents) {
                    val window = document.toObject(Window::class.java)
                    window.id = document.id
                    windowList.add(window)
                }

                updateWindowTabs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading windows: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadFloorSpaces() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("floorSpaces")
            .get()
            .addOnSuccessListener { documents ->
                floorSpaceList.clear()

                for (document in documents) {
                    val floorSpace = document.toObject(FloorSpace::class.java)
                    floorSpace.id = document.id
                    floorSpaceList.add(floorSpace)
                }

                updateFloorTabs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateWindowTabs() {
        ui.btnWindowTab1.visibility = View.GONE
        ui.btnWindowTab2.visibility = View.GONE
        ui.btnWindowTab3.visibility = View.GONE

        if (windowList.isEmpty()) {
            ui.tvWindowsInRoom.text = "No windows in this room"
            ui.tvApplyWindowTitle.text = "No window available"
            return
        }

        selectedItemIndex = 0
        ui.tvWindowsInRoom.text = "Windows in this room:"

        ui.btnWindowTab1.text = windowList.getOrNull(0)?.name ?: ""
        ui.btnWindowTab2.text = windowList.getOrNull(1)?.name ?: ""
        ui.btnWindowTab3.text = windowList.getOrNull(2)?.name ?: ""

        ui.btnWindowTab1.visibility = if (windowList.size >= 1) View.VISIBLE else View.GONE
        ui.btnWindowTab2.visibility = if (windowList.size >= 2) View.VISIBLE else View.GONE
        ui.btnWindowTab3.visibility = if (windowList.size >= 3) View.VISIBLE else View.GONE

        ui.btnWindowTab1.setOnClickListener {
            selectedItemIndex = 0
            updateSelectedWindowDisplay()
        }

        ui.btnWindowTab2.setOnClickListener {
            selectedItemIndex = 1
            updateSelectedWindowDisplay()
        }

        ui.btnWindowTab3.setOnClickListener {
            selectedItemIndex = 2
            updateSelectedWindowDisplay()
        }

        updateSelectedWindowDisplay()
    }

    private fun updateFloorTabs() {
        ui.btnWindowTab1.visibility = View.GONE
        ui.btnWindowTab2.visibility = View.GONE
        ui.btnWindowTab3.visibility = View.GONE

        if (floorSpaceList.isEmpty()) {
            ui.tvWindowsInRoom.text = "No floor spaces in this room"
            ui.tvApplyWindowTitle.text = "No floor space available"
            return
        }

        selectedItemIndex = 0
        ui.tvWindowsInRoom.text = "Floor in this room:"

        ui.btnWindowTab1.text = floorSpaceList.getOrNull(0)?.name ?: ""
        ui.btnWindowTab2.text = floorSpaceList.getOrNull(1)?.name ?: ""
        ui.btnWindowTab3.text = floorSpaceList.getOrNull(2)?.name ?: ""

        ui.btnWindowTab1.visibility = if (floorSpaceList.size >= 1) View.VISIBLE else View.GONE
        ui.btnWindowTab2.visibility = if (floorSpaceList.size >= 2) View.VISIBLE else View.GONE
        ui.btnWindowTab3.visibility = if (floorSpaceList.size >= 3) View.VISIBLE else View.GONE

        ui.btnWindowTab1.setOnClickListener {
            selectedItemIndex = 0
            updateSelectedFloorDisplay()
        }

        ui.btnWindowTab2.setOnClickListener {
            selectedItemIndex = 1
            updateSelectedFloorDisplay()
        }

        ui.btnWindowTab3.setOnClickListener {
            selectedItemIndex = 2
            updateSelectedFloorDisplay()
        }

        updateSelectedFloorDisplay()
    }

    private fun updateSelectedWindowDisplay() {
        val window = windowList.getOrNull(selectedItemIndex) ?: return
        ui.tvApplyWindowTitle.text =
            "Apply to ${window.name} (${window.width} × ${window.height} mm)"
    }

    private fun updateSelectedFloorDisplay() {
        val floor = floorSpaceList.getOrNull(selectedItemIndex) ?: return
        ui.tvApplyWindowTitle.text =
            "Apply to ${floor.name} (${floor.width} × ${floor.length} mm)"
    }

    private fun updateSelectionModeUI() {
        val choosingDifferent = ui.rbChooseDifferent.isChecked

        ui.recyclerProducts.alpha = if (choosingDifferent) 1.0f else 0.5f
        ui.tvOtherProductsHint.text = if (choosingDifferent) {
            "Tap a product below to select from this list"
        } else {
            "Choose 'Different Product' above to select from this list"
        }
    }

    private fun getWindowConstraintFailureReason(
        product: Product,
        width: Int,
        height: Int
    ): String? {
        val minHeight = product.minHeight
        val maxHeight = product.maxHeight
        val minWidth = product.minWidth
        val maxWidth = product.maxWidth
        val maxPanels = product.maxPanels ?: 1

        if (minHeight != null && height < minHeight) {
            return "${product.name} is not allowed: height is below minimum (${minHeight} mm)"
        }

        if (maxHeight != null && height > maxHeight) {
            return "${product.name} is not allowed: height is above maximum (${maxHeight} mm)"
        }

        if (minWidth == null || maxWidth == null) {
            return null
        }

        if (minWidth == maxWidth) {
            val exactPanelWidth = minWidth

            if (width % exactPanelWidth != 0) {
                return "${product.name} is not allowed: width must be an exact multiple of ${exactPanelWidth} mm"
            }

            val requiredPanels = width / exactPanelWidth
            if (requiredPanels <= 0 || requiredPanels > maxPanels) {
                return "${product.name} is not allowed: requires $requiredPanels panels, but maximum allowed is $maxPanels"
            }

            return null
        }

        if (width < minWidth) {
            return "${product.name} is not allowed: width is below minimum (${minWidth} mm)"
        }

        if (width <= maxWidth) {
            return null
        }

        if (maxPanels <= 1) {
            return "${product.name} is not allowed: width exceeds max width (${maxWidth} mm) and this product only supports one panel"
        }

        for (panels in 2..maxPanels) {
            val panelWidth = width.toDouble() / panels.toDouble()
            if (panelWidth >= minWidth && panelWidth <= maxWidth) {
                return null
            }
        }

        return "${product.name} is not allowed: width cannot be split into valid panels within the allowed range (${minWidth}-${maxWidth} mm) and max panel count ($maxPanels)"
    }

    private fun getProductImage(productName: String): Int {
        return when (productName.lowercase()) {
            "standard roller blind" -> R.drawable.roller_blind
            "modular vertical slat" -> R.drawable.venetian_blind
            "fixed-width plantation shutter" -> R.drawable.shutter
            "extra wide sheer curtain" -> R.drawable.curtain
            "thermal blackout blind" -> R.drawable.roller_blind
            "cafe-style half shutter" -> R.drawable.shutter
            "slimline aluminium venetian" -> R.drawable.venetian_blind
            "velvet theater drape" -> R.drawable.curtain
            "bamboo eco-roll" -> R.drawable.roller_blind
            "industrial skylight blind" -> R.drawable.roller_blind
            "premium wool carpet" -> R.drawable.vinyl_flooring
            "commercial grade nylon" -> R.drawable.laminate_flooring
            "engineered oak floorboards" -> R.drawable.timber_flooring
            "recycled rubber gym floor" -> R.drawable.tiles
            "luxury vinyl plank" -> R.drawable.vinyl_flooring
            "polished concrete tiles" -> R.drawable.tiles
            "berber loop special" -> R.drawable.laminate_flooring
            "eco-cork floating floor" -> R.drawable.timber_flooring
            "parquetry herringbone oak" -> R.drawable.timber_flooring
            "pet-proof synthetic turf" -> R.drawable.vinyl_flooring
            else -> android.R.drawable.ic_menu_gallery
        }
    }

    private fun returnSelectedProduct(product: Product, applyToAllSelection: Boolean) {
        val resultIntent = Intent()
        resultIntent.putExtra("selectedProductId", product.id)
        resultIntent.putExtra("selectedProductName", product.name)
        resultIntent.putExtra("selectedProductPricePerSqm", product.pricePerSqm)
        resultIntent.putExtra("applyToAllSelection", applyToAllSelection)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
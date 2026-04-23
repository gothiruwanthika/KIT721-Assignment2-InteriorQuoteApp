package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivitySelectProductBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SelectProductActivity : AppCompatActivity() {

    private lateinit var ui: ActivitySelectProductBinding
    private var selectedProduct: Product? = null
    private var selectionType: String = "window"
    private var houseId: String = ""
    private var roomId: String = ""

    private var windowList = mutableListOf<Window>()
    private var floorSpaceList = mutableListOf<FloorSpace>()
    private var selectedItemIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(ui.root)

        selectionType = intent.getStringExtra("selectionType") ?: "window"
        houseId = intent.getStringExtra("houseId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""

        ui.btnBack.setOnClickListener {
            finish()
        }

        val products = if (selectionType == "floor") {
            listOf(
                Product("Vinyl Flooring", "Durable and water resistant", "$40 / m²"),
                Product("Laminate Flooring", "Affordable and easy to maintain", "$35 / m²"),
                Product("Timber Flooring", "Natural wood finish", "$90 / m²"),
                Product("Tiles", "Strong and moisture resistant", "$55 / m²")
            )
        } else {
            listOf(
                Product("Roller Blind", "Simple and effective light control", "$50 / m²"),
                Product("Venetian Blind", "Adjustable slats for light and privacy", "$65 / m²"),
                Product("Curtain", "Soft furnishing for privacy and decoration", "$80 / m²"),
                Product("Shutter", "Solid window covering with strong privacy", "$120 / m²")
            )
        }

        selectedProduct = products[0]

        val adapter = ProductAdapter(products) { product ->
            selectedProduct = product
            ui.tvRecommendedProductName.text = product.name
            ui.tvRecommendedProductPrice.text = product.priceText
            ui.tvRecommendedSummary.text = "${product.name}, ${product.priceText}"
        }

        ui.recyclerProducts.layoutManager = LinearLayoutManager(this)
        ui.recyclerProducts.adapter = adapter

        ui.btnSaveProduct.setOnClickListener {
            val product = selectedProduct
            if (product == null) {
                Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show()
            } else {
                returnSelectedProduct(product)
            }
        }

        ui.btnApplyToAllWindows.setOnClickListener {
            Toast.makeText(this, "Apply to all will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.switchApplySameSelection.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Apply same selection logic will be added later", Toast.LENGTH_SHORT).show()
        }

        loadRoomItems()
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
                    ui.tvRecommendedProductName.text = "Vinyl Flooring"
                    ui.tvRecommendedProductPrice.text = "$40 / m² · Durable and water resistant"
                    ui.tvRecommendedNote.text = "Suggested for all floor spaces in this room"
                    ui.rbChooseDifferent.text = "Choose Different Product for this Floor Space"
                    ui.tvRecommendedSummary.text = "Vinyl Flooring, $40 / m²"
                    ui.tvApplySameSelectionLabel.text = "Apply same selection to all floor in this room"
                } else {
                    ui.tvWindowsInRoom.text = "Windows in this room:"
                    ui.tvRecommendedProductName.text = "Roller Blind"
                    ui.tvRecommendedProductPrice.text = "$50 / m² · Best for light control"
                    ui.tvRecommendedNote.text = "Suggested for all windows in this room"
                    ui.rbChooseDifferent.text = "Choose Different Product for this Window"
                    ui.tvRecommendedSummary.text = "Roller Blind, $50 / m²"
                    ui.tvApplySameSelectionLabel.text = "Apply same selection to all windows in this room"
                }

                if (selectionType == "floor") {
                    loadFloorSpaces()
                } else {
                    loadWindows()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading room: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
                updateSelectedWindowDisplay()
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
                updateSelectedFloorDisplay()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateWindowTabs() {
        if (windowList.isEmpty()) return

        selectedItemIndex = 0

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
    }

    private fun updateFloorTabs() {
        if (floorSpaceList.isEmpty()) return

        selectedItemIndex = 0

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
    }

    private fun updateSelectedWindowDisplay() {
        val window = windowList.getOrNull(selectedItemIndex) ?: return

        ui.tvApplyWindowTitle.text =
            "Apply to ${window.name} (${window.width} × ${window.height} mm)"

        ui.tvApplySameSelectionHint.text =
            "This will update the other windows in this room with the same product"
    }

    private fun updateSelectedFloorDisplay() {
        val floor = floorSpaceList.getOrNull(selectedItemIndex) ?: return

        ui.tvApplyWindowTitle.text =
            "Apply to ${floor.name} (${floor.width} × ${floor.length} mm)"

        ui.tvApplySameSelectionHint.text =
            "This will update the other floor spaces in this room with the same product"
    }

    private fun returnSelectedProduct(product: Product) {
        val resultIntent = Intent()
        resultIntent.putExtra("selectedProductName", product.name)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
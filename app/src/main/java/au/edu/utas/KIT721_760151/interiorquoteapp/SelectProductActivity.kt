package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivitySelectProductBinding

class SelectProductActivity : AppCompatActivity() {

    private lateinit var ui: ActivitySelectProductBinding
    private var selectedProduct: Product? = null
    private var selectionType: String = "window"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(ui.root)

        selectionType = intent.getStringExtra("selectionType") ?: "window"

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

        if (selectionType == "floor") {
            ui.tvRoomInfo.text = "Room : Living Room"
            ui.tvWindowsInRoom.text = "Floor in this room:"
            ui.btnWindowTab1.text = "Floor 1"
            ui.btnWindowTab2.text = "Floor 2"
            ui.btnWindowTab3.visibility = View.GONE

            ui.tvRecommendedProductName.text = "Vinyl Flooring"
            ui.tvRecommendedProductPrice.text = "$40 / m² · Durable and water resistant"
            ui.tvRecommendedNote.text = "Suggested for all floor spaces in this room"
            ui.tvApplyWindowTitle.text = "Apply to Floor 1 (3500 × 4000 mm)"
            ui.rbChooseDifferent.text = "Choose Different Product for this Floor Space"
            ui.tvRecommendedSummary.text = "Vinyl Flooring, $40 / m²"
            ui.tvApplySameSelectionLabel.text = "Apply same selection to all floor in this room"
            ui.tvApplySameSelectionHint.text = "This will update floor 2 with the same product"
        } else {
            ui.tvRoomInfo.text = "Room : Living Room"
            ui.tvWindowsInRoom.text = "Windows in this room:"
            ui.btnWindowTab1.text = "Window 1"
            ui.btnWindowTab2.text = "Window 2"
            ui.btnWindowTab3.visibility = View.VISIBLE
            ui.btnWindowTab3.text = "Window 3"

            ui.tvRecommendedProductName.text = "Roller Blind"
            ui.tvRecommendedProductPrice.text = "$50 / m² · Best for light control"
            ui.tvRecommendedNote.text = "Suggested for all windows in this room"
            ui.tvApplyWindowTitle.text = "Apply to Window 1 (1200 × 1500 mm)"
            ui.rbChooseDifferent.text = "Choose Different Product for this Window"
            ui.tvRecommendedSummary.text = "Roller Blind, $50 / m²"
            ui.tvApplySameSelectionLabel.text = "Apply same selection to all windows in this room"
            ui.tvApplySameSelectionHint.text = "This will update window 2 & window 3 with the same product"
        }

        val adapter = ProductAdapter(products) { product ->
            selectedProduct = product
            ui.tvRecommendedProductName.text = product.name
            ui.tvRecommendedProductPrice.text = product.priceText
            ui.tvRecommendedSummary.text = "${product.name}, ${product.priceText}"

            returnSelectedProduct(product)
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

        ui.btnWindowTab1.setOnClickListener {
            Toast.makeText(this, "Tab switching will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.btnWindowTab2.setOnClickListener {
            Toast.makeText(this, "Tab switching will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.btnWindowTab3.setOnClickListener {
            Toast.makeText(this, "Tab switching will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.switchApplySameSelection.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Apply same selection logic will be added later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnSelectedProduct(product: Product) {
        val resultIntent = Intent()
        resultIntent.putExtra("selectedProductName", product.name)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
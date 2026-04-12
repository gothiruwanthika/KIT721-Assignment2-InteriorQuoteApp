package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivitySelectProductBinding

class SelectProductActivity : AppCompatActivity() {

    private lateinit var ui: ActivitySelectProductBinding
    private var selectedProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivitySelectProductBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnBack.setOnClickListener {
            finish()
        }

        val products = listOf(
            Product("Roller Blind", "Simple and effective light control", "$50 / m²"),
            Product("Venetian Blind", "Adjustable slats for light and privacy", "$65 / m²"),
            Product("Curtain", "Soft furnishing for privacy and decoration", "$80 / m²"),
            Product("Shutter", "Solid window covering with strong privacy", "$120 / m²")
        )

        selectedProduct = products[0]

        ui.tvRoomInfo.text = "Room : Living Room"
        ui.tvWindowsInRoom.text = "Windows in this room:"
        ui.tvRecommendedProductName.text = selectedProduct!!.name
        ui.tvRecommendedProductPrice.text = "${selectedProduct!!.priceText} · Best for light control"
        ui.tvRecommendedNote.text = "Suggested for all windows in this room"
        ui.tvRecommendedSummary.text = "${selectedProduct!!.name}, ${selectedProduct!!.priceText}"

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
            Toast.makeText(this, "Apply to all windows will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.btnWindowTab1.setOnClickListener {
            Toast.makeText(this, "Window tab switching will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.btnWindowTab2.setOnClickListener {
            Toast.makeText(this, "Window tab switching will be added later", Toast.LENGTH_SHORT).show()
        }

        ui.btnWindowTab3.setOnClickListener {
            Toast.makeText(this, "Window tab switching will be added later", Toast.LENGTH_SHORT).show()
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
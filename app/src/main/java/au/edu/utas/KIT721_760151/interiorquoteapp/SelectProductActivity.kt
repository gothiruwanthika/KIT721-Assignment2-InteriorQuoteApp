package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivitySelectProductBinding

class SelectProductActivity : AppCompatActivity() {

    private lateinit var ui: ActivitySelectProductBinding

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

        val adapter = ProductAdapter(products) { selectedProduct ->
            val resultIntent = Intent()
            resultIntent.putExtra("selectedProductName", selectedProduct.name)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        ui.recyclerProducts.layoutManager = LinearLayoutManager(this)
        ui.recyclerProducts.adapter = adapter
    }
}
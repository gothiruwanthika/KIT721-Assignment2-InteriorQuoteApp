package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityHouseDetailsBinding

class HouseDetailsActivity : AppCompatActivity() {

    private lateinit var ui: ActivityHouseDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityHouseDetailsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnBack.setOnClickListener {
            finish()
        }

        val houseId = intent.getStringExtra("houseId") ?: ""
        val customerName = intent.getStringExtra("customerName") ?: ""
        val addressLine1 = intent.getStringExtra("addressLine1") ?: ""
        val city = intent.getStringExtra("city") ?: ""

        ui.tvTitle.text = customerName
        ui.tvCustomerName.text = customerName

        val cityPart = if (city.isNotBlank()) ", $city" else ""
        ui.tvAddress.text = "$addressLine1$cityPart"

        ui.btnEditHouse.setOnClickListener {
            Toast.makeText(this, "Edit house clicked", Toast.LENGTH_SHORT).show()
        }

        ui.btnAddRoom.setOnClickListener {
            Toast.makeText(this, "Add Room clicked", Toast.LENGTH_SHORT).show()
        }

        ui.btnGenerateQuote.setOnClickListener {
            Toast.makeText(this, "Generate Quote clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
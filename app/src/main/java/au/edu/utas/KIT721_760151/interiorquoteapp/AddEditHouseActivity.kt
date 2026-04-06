package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditHouseBinding

class AddEditHouseActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditHouseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditHouseBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnSaveHouse.setOnClickListener {
            saveHouse()
        }
    }

    private fun saveHouse() {
        val customerName = ui.etCustomerName.text.toString().trim()
        val contactNumber = ui.etContactNumber.text.toString().trim()
        val addressLine1 = ui.etAddressLine1.text.toString().trim()
        val addressLine2 = ui.etAddressLine2.text.toString().trim()
        val city = ui.etCity.text.toString().trim()
        val postalCode = ui.etPostalCode.text.toString().trim()
        val email = ui.etEmail.text.toString().trim()

        if (customerName.isEmpty()) {
            ui.etCustomerName.error = "Customer name is required"
            ui.etCustomerName.requestFocus()
            return
        }

        if (contactNumber.isEmpty()) {
            ui.etContactNumber.error = "Contact number is required"
            ui.etContactNumber.requestFocus()
            return
        }

        if (addressLine1.isEmpty()) {
            ui.etAddressLine1.error = "Address is required"
            ui.etAddressLine1.requestFocus()
            return
        }

        val house = House(
            customerName = customerName,
            contactNumber = contactNumber,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            postalCode = postalCode,
            email = email
        )

        Toast.makeText(this, "House form is valid", Toast.LENGTH_SHORT).show()
        finish()
    }
}
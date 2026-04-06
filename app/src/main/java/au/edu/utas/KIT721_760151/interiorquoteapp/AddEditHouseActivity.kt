package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditHouseBinding

class AddEditHouseActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditHouseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditHouseBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnBack.setOnClickListener {
            finish()
        }

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

        ui.tvCustomerNameError.visibility = View.GONE
        ui.tvContactNumberError.visibility = View.GONE
        ui.tvAddressLine1Error.visibility = View.GONE

        ui.tvCustomerNameError.text = "Customer name is required"
        ui.tvContactNumberError.text = "Contact number is required"
        ui.tvAddressLine1Error.text = "Address line 1 is required"

        if (customerName.isEmpty()) {
            ui.tvCustomerNameError.visibility = View.VISIBLE
            ui.etCustomerName.requestFocus()
            return
        }

        if (contactNumber.isEmpty()) {
            ui.tvContactNumberError.visibility = View.VISIBLE
            ui.etContactNumber.requestFocus()
            return
        }

        if (!contactNumber.matches(Regex("^\\d{10}$"))) {
            ui.tvContactNumberError.text = "Enter a valid 10-digit contact number"
            ui.tvContactNumberError.visibility = View.VISIBLE
            ui.etContactNumber.requestFocus()
            return
        }

        if (addressLine1.isEmpty()) {
            ui.tvAddressLine1Error.visibility = View.VISIBLE
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

        val db = Firebase.firestore
        db.collection("houses")
            .add(house)
            .addOnSuccessListener {
                Toast.makeText(this, "House saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving house: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
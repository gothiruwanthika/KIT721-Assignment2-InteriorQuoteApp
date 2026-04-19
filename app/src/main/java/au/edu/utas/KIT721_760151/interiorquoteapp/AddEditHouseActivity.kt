package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditHouseBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddEditHouseActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditHouseBinding
    private var houseId: String = ""
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditHouseBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        isEditMode = intent.getBooleanExtra("isEdit", false)

        if (isEditMode) {
            ui.btnSaveHouse.text = "Update"

            ui.etCustomerName.setText(intent.getStringExtra("customerName") ?: "")
            ui.etContactNumber.setText(intent.getStringExtra("contactNumber") ?: "")
            ui.etAddressLine1.setText(intent.getStringExtra("addressLine1") ?: "")
            ui.etAddressLine2.setText(intent.getStringExtra("addressLine2") ?: "")
            ui.etCity.setText(intent.getStringExtra("city") ?: "")
            ui.etPostalCode.setText(intent.getStringExtra("postalCode") ?: "")
            ui.etEmail.setText(intent.getStringExtra("email") ?: "")
        }

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnSaveHouse.setOnClickListener {
            if (isEditMode) {
                updateHouse()
            } else {
                saveHouse()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val customerName = ui.etCustomerName.text.toString().trim()
        val contactNumber = ui.etContactNumber.text.toString().trim()
        val addressLine1 = ui.etAddressLine1.text.toString().trim()

        ui.tvCustomerNameError.visibility = View.GONE
        ui.tvContactNumberError.visibility = View.GONE
        ui.tvAddressLine1Error.visibility = View.GONE

        if (customerName.isEmpty()) {
            ui.tvCustomerNameError.visibility = View.VISIBLE
            ui.etCustomerName.requestFocus()
            return false
        }

        if (contactNumber.isEmpty()) {
            ui.tvContactNumberError.text = "Contact number is required"
            ui.tvContactNumberError.visibility = View.VISIBLE
            ui.etContactNumber.requestFocus()
            return false
        }

        if (!contactNumber.matches(Regex("^\\d{10}$"))) {
            ui.tvContactNumberError.text = "Enter a valid 10-digit contact number"
            ui.tvContactNumberError.visibility = View.VISIBLE
            ui.etContactNumber.requestFocus()
            return false
        }

        if (addressLine1.isEmpty()) {
            ui.tvAddressLine1Error.visibility = View.VISIBLE
            ui.etAddressLine1.requestFocus()
            return false
        }

        return true
    }

    private fun saveHouse() {
        if (!validateInputs()) return

        val house = hashMapOf(
            "customerName" to ui.etCustomerName.text.toString().trim(),
            "contactNumber" to ui.etContactNumber.text.toString().trim(),
            "addressLine1" to ui.etAddressLine1.text.toString().trim(),
            "addressLine2" to ui.etAddressLine2.text.toString().trim(),
            "city" to ui.etCity.text.toString().trim(),
            "postalCode" to ui.etPostalCode.text.toString().trim(),
            "email" to ui.etEmail.text.toString().trim()
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

    private fun updateHouse() {
        if (!validateInputs()) return

        if (houseId.isBlank()) {
            Toast.makeText(this, "Missing house ID", Toast.LENGTH_LONG).show()
            return
        }

        val updates = mapOf(
            "customerName" to ui.etCustomerName.text.toString().trim(),
            "contactNumber" to ui.etContactNumber.text.toString().trim(),
            "addressLine1" to ui.etAddressLine1.text.toString().trim(),
            "addressLine2" to ui.etAddressLine2.text.toString().trim(),
            "city" to ui.etCity.text.toString().trim(),
            "postalCode" to ui.etPostalCode.text.toString().trim(),
            "email" to ui.etEmail.text.toString().trim()
        )

        val db = Firebase.firestore
        db.collection("houses")
            .document(houseId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "House updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating house: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
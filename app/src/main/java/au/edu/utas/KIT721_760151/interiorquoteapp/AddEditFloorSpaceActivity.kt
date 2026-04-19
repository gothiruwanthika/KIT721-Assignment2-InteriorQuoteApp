package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditFloorSpaceBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddEditFloorSpaceActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditFloorSpaceBinding
    private var houseId: String = ""
    private var roomId: String = ""
    private var floorSpaceId: String = ""
    private var isEditMode: Boolean = false

    private var selectedProductName: String = ""

    private val productSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedProductName = data?.getStringExtra("selectedProductName") ?: ""
                ui.tvSelectedProduct.text = if (selectedProductName.isBlank()) {
                    "No product selected"
                } else {
                    selectedProductName
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditFloorSpaceBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""
        floorSpaceId = intent.getStringExtra("floorSpaceId") ?: ""
        isEditMode = intent.getBooleanExtra("isEdit", false)

        if (isEditMode) {
            ui.btnSaveFloorSpace.text = "Update"

            ui.etFloorSpaceName.setText(intent.getStringExtra("floorSpaceName") ?: "")
            ui.etFloorSpaceWidth.setText(intent.getStringExtra("width") ?: "")
            ui.etFloorSpaceLength.setText(intent.getStringExtra("length") ?: "")

            selectedProductName = intent.getStringExtra("selectedProductName") ?: ""
            ui.tvSelectedProduct.text = if (selectedProductName.isBlank()) {
                "No product selected"
            } else {
                selectedProductName
            }
        }

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnSaveFloorSpace.setOnClickListener {
            if (isEditMode) {
                updateFloorSpace()
            } else {
                saveFloorSpace()
            }
        }

        ui.layoutSelectedProduct.setOnClickListener {
            val intent = Intent(this, SelectProductActivity::class.java)
            intent.putExtra("selectionType", "floor")
            productSelectionLauncher.launch(intent)
        }
    }

    private fun saveFloorSpace() {
        val floorSpaceName = ui.etFloorSpaceName.text.toString().trim()
        val widthText = ui.etFloorSpaceWidth.text.toString().trim()
        val lengthText = ui.etFloorSpaceLength.text.toString().trim()

        ui.tvFloorSpaceNameError.visibility = View.GONE
        ui.tvFloorSpaceWidthError.visibility = View.GONE
        ui.tvFloorSpaceLengthError.visibility = View.GONE
        ui.tvSelectedProductError.visibility = View.GONE

        ui.tvFloorSpaceNameError.text = "Floor space name is required"
        ui.tvFloorSpaceWidthError.text = "Width is required"
        ui.tvFloorSpaceLengthError.text = "Length is required"
        ui.tvSelectedProductError.text = "Product is required"

        if (floorSpaceName.isEmpty()) {
            ui.tvFloorSpaceNameError.visibility = View.VISIBLE
            ui.etFloorSpaceName.requestFocus()
            return
        }

        if (widthText.isEmpty()) {
            ui.tvFloorSpaceWidthError.visibility = View.VISIBLE
            ui.etFloorSpaceWidth.requestFocus()
            return
        }

        if (lengthText.isEmpty()) {
            ui.tvFloorSpaceLengthError.visibility = View.VISIBLE
            ui.etFloorSpaceLength.requestFocus()
            return
        }

        val width = widthText.toIntOrNull()
        if (width == null || width <= 0) {
            ui.tvFloorSpaceWidthError.text = "Enter a valid width"
            ui.tvFloorSpaceWidthError.visibility = View.VISIBLE
            ui.etFloorSpaceWidth.requestFocus()
            return
        }

        val length = lengthText.toIntOrNull()
        if (length == null || length <= 0) {
            ui.tvFloorSpaceLengthError.text = "Enter a valid length"
            ui.tvFloorSpaceLengthError.visibility = View.VISIBLE
            ui.etFloorSpaceLength.requestFocus()
            return
        }

        if (selectedProductName.isBlank()) {
            ui.tvSelectedProductError.visibility = View.VISIBLE
            ui.layoutSelectedProduct.requestFocus()
            return
        }

        if (houseId.isBlank() || roomId.isBlank()) {
            Toast.makeText(this, "Missing house or room ID", Toast.LENGTH_LONG).show()
            return
        }

        val floorSpaceData = hashMapOf(
            "name" to floorSpaceName,
            "width" to width,
            "length" to length,
            "productName" to selectedProductName,
            "houseId" to houseId,
            "roomId" to roomId
        )

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("floorSpaces")
            .add(floorSpaceData)
            .addOnSuccessListener {
                Toast.makeText(this, "Floor space saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving floor space: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateFloorSpace() {
        val floorSpaceName = ui.etFloorSpaceName.text.toString().trim()
        val widthText = ui.etFloorSpaceWidth.text.toString().trim()
        val lengthText = ui.etFloorSpaceLength.text.toString().trim()

        ui.tvFloorSpaceNameError.visibility = View.GONE
        ui.tvFloorSpaceWidthError.visibility = View.GONE
        ui.tvFloorSpaceLengthError.visibility = View.GONE
        ui.tvSelectedProductError.visibility = View.GONE

        ui.tvFloorSpaceNameError.text = "Floor space name is required"
        ui.tvFloorSpaceWidthError.text = "Width is required"
        ui.tvFloorSpaceLengthError.text = "Length is required"
        ui.tvSelectedProductError.text = "Product is required"

        if (floorSpaceName.isEmpty()) {
            ui.tvFloorSpaceNameError.visibility = View.VISIBLE
            ui.etFloorSpaceName.requestFocus()
            return
        }

        if (widthText.isEmpty()) {
            ui.tvFloorSpaceWidthError.visibility = View.VISIBLE
            ui.etFloorSpaceWidth.requestFocus()
            return
        }

        if (lengthText.isEmpty()) {
            ui.tvFloorSpaceLengthError.visibility = View.VISIBLE
            ui.etFloorSpaceLength.requestFocus()
            return
        }

        val width = widthText.toIntOrNull()
        if (width == null || width <= 0) {
            ui.tvFloorSpaceWidthError.text = "Enter a valid width"
            ui.tvFloorSpaceWidthError.visibility = View.VISIBLE
            ui.etFloorSpaceWidth.requestFocus()
            return
        }

        val length = lengthText.toIntOrNull()
        if (length == null || length <= 0) {
            ui.tvFloorSpaceLengthError.text = "Enter a valid length"
            ui.tvFloorSpaceLengthError.visibility = View.VISIBLE
            ui.etFloorSpaceLength.requestFocus()
            return
        }

        if (selectedProductName.isBlank()) {
            ui.tvSelectedProductError.visibility = View.VISIBLE
            ui.layoutSelectedProduct.requestFocus()
            return
        }

        if (houseId.isBlank() || roomId.isBlank() || floorSpaceId.isBlank()) {
            Toast.makeText(this, "Missing floor space details", Toast.LENGTH_LONG).show()
            return
        }

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("floorSpaces").document(floorSpaceId)
            .update(
                mapOf(
                    "name" to floorSpaceName,
                    "width" to width,
                    "length" to length,
                    "productName" to selectedProductName
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Floor space updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating floor space: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
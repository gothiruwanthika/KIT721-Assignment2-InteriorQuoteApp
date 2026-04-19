package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditWindowBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddEditWindowActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditWindowBinding
    private var houseId: String = ""
    private var roomId: String = ""
    private var windowId: String = ""
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
        ui = ActivityAddEditWindowBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""
        windowId = intent.getStringExtra("windowId") ?: ""
        isEditMode = intent.getBooleanExtra("isEdit", false)

        if (isEditMode) {
            ui.btnSaveWindow.text = "Update"

            ui.etWindowName.setText(intent.getStringExtra("windowName") ?: "")
            ui.etWindowWidth.setText(intent.getStringExtra("width") ?: "")
            ui.etWindowHeight.setText(intent.getStringExtra("height") ?: "")

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

        ui.btnSaveWindow.setOnClickListener {
            if (isEditMode) {
                updateWindow()
            } else {
                saveWindow()
            }
        }

        ui.layoutSelectedProduct.setOnClickListener {
            val intent = Intent(this, SelectProductActivity::class.java)
            intent.putExtra("selectionType", "window")
            productSelectionLauncher.launch(intent)
        }
    }

    private fun saveWindow() {
        val windowName = ui.etWindowName.text.toString().trim()
        val widthText = ui.etWindowWidth.text.toString().trim()
        val heightText = ui.etWindowHeight.text.toString().trim()

        ui.tvWindowNameError.visibility = View.GONE
        ui.tvWindowWidthError.visibility = View.GONE
        ui.tvWindowHeightError.visibility = View.GONE
        ui.tvSelectedProductError.visibility = View.GONE

        ui.tvWindowNameError.text = "Window name is required"
        ui.tvWindowWidthError.text = "Width is required"
        ui.tvWindowHeightError.text = "Height is required"
        ui.tvSelectedProductError.text = "Product is required"

        if (windowName.isEmpty()) {
            ui.tvWindowNameError.visibility = View.VISIBLE
            ui.etWindowName.requestFocus()
            return
        }

        if (widthText.isEmpty()) {
            ui.tvWindowWidthError.visibility = View.VISIBLE
            ui.etWindowWidth.requestFocus()
            return
        }

        if (heightText.isEmpty()) {
            ui.tvWindowHeightError.visibility = View.VISIBLE
            ui.etWindowHeight.requestFocus()
            return
        }

        val width = widthText.toIntOrNull()
        if (width == null || width <= 0) {
            ui.tvWindowWidthError.text = "Enter a valid width"
            ui.tvWindowWidthError.visibility = View.VISIBLE
            ui.etWindowWidth.requestFocus()
            return
        }

        val height = heightText.toIntOrNull()
        if (height == null || height <= 0) {
            ui.tvWindowHeightError.text = "Enter a valid height"
            ui.tvWindowHeightError.visibility = View.VISIBLE
            ui.etWindowHeight.requestFocus()
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

        val windowData = hashMapOf(
            "name" to windowName,
            "width" to width,
            "height" to height,
            "productName" to selectedProductName,
            "houseId" to houseId,
            "roomId" to roomId
        )

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("windows")
            .add(windowData)
            .addOnSuccessListener {
                Toast.makeText(this, "Window saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving window: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateWindow() {
        val windowName = ui.etWindowName.text.toString().trim()
        val widthText = ui.etWindowWidth.text.toString().trim()
        val heightText = ui.etWindowHeight.text.toString().trim()

        ui.tvWindowNameError.visibility = View.GONE
        ui.tvWindowWidthError.visibility = View.GONE
        ui.tvWindowHeightError.visibility = View.GONE
        ui.tvSelectedProductError.visibility = View.GONE

        ui.tvWindowNameError.text = "Window name is required"
        ui.tvWindowWidthError.text = "Width is required"
        ui.tvWindowHeightError.text = "Height is required"
        ui.tvSelectedProductError.text = "Product is required"

        if (windowName.isEmpty()) {
            ui.tvWindowNameError.visibility = View.VISIBLE
            ui.etWindowName.requestFocus()
            return
        }

        if (widthText.isEmpty()) {
            ui.tvWindowWidthError.visibility = View.VISIBLE
            ui.etWindowWidth.requestFocus()
            return
        }

        if (heightText.isEmpty()) {
            ui.tvWindowHeightError.visibility = View.VISIBLE
            ui.etWindowHeight.requestFocus()
            return
        }

        val width = widthText.toIntOrNull()
        if (width == null || width <= 0) {
            ui.tvWindowWidthError.text = "Enter a valid width"
            ui.tvWindowWidthError.visibility = View.VISIBLE
            ui.etWindowWidth.requestFocus()
            return
        }

        val height = heightText.toIntOrNull()
        if (height == null || height <= 0) {
            ui.tvWindowHeightError.text = "Enter a valid height"
            ui.tvWindowHeightError.visibility = View.VISIBLE
            ui.etWindowHeight.requestFocus()
            return
        }

        if (selectedProductName.isBlank()) {
            ui.tvSelectedProductError.visibility = View.VISIBLE
            ui.layoutSelectedProduct.requestFocus()
            return
        }

        if (houseId.isBlank() || roomId.isBlank() || windowId.isBlank()) {
            Toast.makeText(this, "Missing window details", Toast.LENGTH_LONG).show()
            return
        }

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("windows").document(windowId)
            .update(
                mapOf(
                    "name" to windowName,
                    "width" to width,
                    "height" to height,
                    "productName" to selectedProductName
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Window updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating window: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditRoomBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddEditRoomActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditRoomBinding
    private var houseId: String = ""
    private var roomId: String = ""
    private var isEditMode: Boolean = false

    private var imageBase64: String = ""

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                handleSelectedImage(uri)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                handleCapturedBitmap(bitmap)
            } else {
                Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditRoomBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""
        isEditMode = intent.getBooleanExtra("isEdit", false)

        if (isEditMode) {
            ui.tvTitle.text = "Edit Room"
            ui.btnSaveRoom.text = "Update"

            val roomName = intent.getStringExtra("roomName") ?: ""
            val labourCost = intent.getStringExtra("labourCost") ?: ""

            ui.etRoomName.setText(roomName)
            ui.etLabourCost.setText(labourCost)

            loadExistingRoomImage()
        }

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnSaveRoom.setOnClickListener {
            if (isEditMode) {
                updateRoom()
            } else {
                saveRoom()
            }
        }

        ui.btnAddPhoto.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_photo_options, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val layoutChooseGallery = view.findViewById<LinearLayout>(R.id.layoutChooseGallery)
        val layoutTakePhoto = view.findViewById<LinearLayout>(R.id.layoutTakePhoto)
        val tvDialogCancel = view.findViewById<TextView>(R.id.tvDialogCancel)

        layoutChooseGallery.setOnClickListener {
            dialog.dismiss()
            imagePickerLauncher.launch("image/*")
        }

        layoutTakePhoto.setOnClickListener {
            dialog.dismiss()
            cameraLauncher.launch(null)
        }

        tvDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val resizedBitmap = resizeBitmap(bitmap, 600)
                imageBase64 = bitmapToBase64(resizedBitmap)

                ui.imgRoomPhotoPlaceholder.setImageBitmap(resizedBitmap)
                ui.tvNoPhoto.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleCapturedBitmap(bitmap: Bitmap) {
        try {
            val resizedBitmap = resizeBitmap(bitmap, 600)
            imageBase64 = bitmapToBase64(resizedBitmap)

            ui.imgRoomPhotoPlaceholder.setImageBitmap(resizedBitmap)
            ui.tvNoPhoto.visibility = View.GONE
        } catch (e: Exception) {
            Toast.makeText(this, "Error capturing image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadExistingRoomImage() {
        if (houseId.isBlank() || roomId.isBlank()) return

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener { document ->
                imageBase64 = document.getString("imageBase64") ?: ""

                if (imageBase64.isNotBlank()) {
                    val bitmap = base64ToBitmap(imageBase64)
                    if (bitmap != null) {
                        ui.imgRoomPhotoPlaceholder.setImageBitmap(bitmap)
                        ui.tvNoPhoto.visibility = View.GONE
                    }
                }
            }
    }

    private fun saveRoom() {
        val roomName = ui.etRoomName.text.toString().trim()
        val labourCostText = ui.etLabourCost.text.toString().trim()

        ui.tvRoomNameError.visibility = View.GONE
        ui.tvLabourCostError.visibility = View.GONE

        ui.tvRoomNameError.text = "Room name is required"
        ui.tvLabourCostError.text = "Labour cost is required"

        if (roomName.isEmpty()) {
            ui.tvRoomNameError.visibility = View.VISIBLE
            ui.etRoomName.requestFocus()
            return
        }

        if (labourCostText.isEmpty()) {
            ui.tvLabourCostError.visibility = View.VISIBLE
            ui.etLabourCost.requestFocus()
            return
        }

        val labourCost = labourCostText.toDoubleOrNull()
        if (labourCost == null || labourCost <= 0) {
            ui.tvLabourCostError.text = "Enter a valid labour cost"
            ui.tvLabourCostError.visibility = View.VISIBLE
            ui.etLabourCost.requestFocus()
            return
        }

        if (houseId.isBlank()) {
            Toast.makeText(this, "Missing house ID", Toast.LENGTH_LONG).show()
            return
        }

        val roomData = hashMapOf(
            "name" to roomName,
            "labourCost" to labourCost,
            "houseId" to houseId,
            "includedInQuote" to true,
            "imageBase64" to imageBase64
        )

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms")
            .add(roomData)
            .addOnSuccessListener {
                Toast.makeText(this, "Room saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving room: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateRoom() {
        val roomName = ui.etRoomName.text.toString().trim()
        val labourCostText = ui.etLabourCost.text.toString().trim()

        ui.tvRoomNameError.visibility = View.GONE
        ui.tvLabourCostError.visibility = View.GONE

        ui.tvRoomNameError.text = "Room name is required"
        ui.tvLabourCostError.text = "Labour cost is required"

        if (roomName.isEmpty()) {
            ui.tvRoomNameError.visibility = View.VISIBLE
            ui.etRoomName.requestFocus()
            return
        }

        if (labourCostText.isEmpty()) {
            ui.tvLabourCostError.visibility = View.VISIBLE
            ui.etLabourCost.requestFocus()
            return
        }

        val labourCost = labourCostText.toDoubleOrNull()
        if (labourCost == null || labourCost <= 0) {
            ui.tvLabourCostError.text = "Enter a valid labour cost"
            ui.tvLabourCostError.visibility = View.VISIBLE
            ui.etLabourCost.requestFocus()
            return
        }

        if (houseId.isBlank() || roomId.isBlank()) {
            Toast.makeText(this, "Missing room details", Toast.LENGTH_LONG).show()
            return
        }

        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms")
            .document(roomId)
            .update(
                mapOf(
                    "name" to roomName,
                    "labourCost" to labourCost,
                    "imageBase64" to imageBase64
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Room updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating room: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
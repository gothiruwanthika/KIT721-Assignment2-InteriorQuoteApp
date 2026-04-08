package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityAddEditRoomBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddEditRoomActivity : AppCompatActivity() {

    private lateinit var ui: ActivityAddEditRoomBinding
    private var houseId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddEditRoomBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnSaveRoom.setOnClickListener {
            saveRoom()
        }

        ui.btnAddPhoto.setOnClickListener {
            Toast.makeText(this, "Photo feature will be added later", Toast.LENGTH_SHORT).show()
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
            "includedInQuote" to true
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
}
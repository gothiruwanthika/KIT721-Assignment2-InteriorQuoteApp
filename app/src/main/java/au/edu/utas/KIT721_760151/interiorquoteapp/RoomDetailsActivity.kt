package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityRoomDetailsBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRoomDetailsBinding
    private var houseId: String = ""
    private var roomId: String = ""
    private var roomImageBase64: String = ""

    private lateinit var windowAdapter: WindowAdapter
    private val windowList = mutableListOf<Window>()

    private lateinit var floorSpaceAdapter: FloorSpaceAdapter
    private val floorSpaceList = mutableListOf<FloorSpace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""
        val roomName = intent.getStringExtra("roomName") ?: ""
        val labourCost = intent.getDoubleExtra("labourCost", 0.0)

        ui.tvTitle.text = roomName
        ui.tvRoomName.text = roomName
        ui.tvLabourCost.text = "Labour cost: $$labourCost"

        setupWindowRecyclerView()
        setupFloorSpaceRecyclerView()

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnEditRoom.setOnClickListener {
            val intent = Intent(this, AddEditRoomActivity::class.java)
            intent.putExtra("houseId", houseId)
            intent.putExtra("roomId", roomId)
            intent.putExtra("roomName", ui.tvRoomName.text.toString())
            intent.putExtra(
                "labourCost",
                ui.tvLabourCost.text.toString().replace("Labour cost: $", "")
            )
            intent.putExtra("isEdit", true)
            startActivity(intent)
        }

        ui.btnAddWindow.setOnClickListener {
            val intent = Intent(this, AddEditWindowActivity::class.java)
            intent.putExtra("houseId", houseId)
            intent.putExtra("roomId", roomId)
            startActivity(intent)
        }

        ui.btnAddFloorSpace.setOnClickListener {
            val intent = Intent(this, AddEditFloorSpaceActivity::class.java)
            intent.putExtra("houseId", houseId)
            intent.putExtra("roomId", roomId)
            startActivity(intent)
        }

        ui.btnDeleteRoom.setOnClickListener {
            showDeleteConfirmation()
        }

        ui.imgRoomPhoto.setOnClickListener {
            if (roomImageBase64.isNotBlank()) {
                val intent = Intent(this, RoomImagePreviewActivity::class.java)
                intent.putExtra("roomName", ui.tvRoomName.text.toString())
                intent.putExtra("imageBase64", roomImageBase64)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No image to preview", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRoomDetails()
        loadWindows()
        loadFloorSpaces()
    }

    private fun loadRoomDetails() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roomName = document.getString("name") ?: ""
                    val labourCost = document.getDouble("labourCost") ?: 0.0
                    val imageBase64 = document.getString("imageBase64") ?: ""

                    roomImageBase64 = imageBase64

                    ui.tvTitle.text = roomName
                    ui.tvRoomName.text = roomName
                    ui.tvLabourCost.text = "Labour cost: $$labourCost"

                    if (imageBase64.isNotBlank()) {
                        try {
                            val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            ui.imgRoomPhoto.setImageBitmap(bitmap)
                            ui.tvNoPhoto.visibility = View.GONE
                        } catch (e: Exception) {
                            ui.imgRoomPhoto.setImageResource(android.R.drawable.ic_menu_camera)
                            ui.tvNoPhoto.visibility = View.VISIBLE
                        }
                    } else {
                        ui.imgRoomPhoto.setImageResource(android.R.drawable.ic_menu_camera)
                        ui.tvNoPhoto.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading room details: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupWindowRecyclerView() {
        windowAdapter = WindowAdapter(windowList) { selectedWindow ->
            showWindowOptionsDialog(selectedWindow)
        }

        ui.recyclerWindows.apply {
            layoutManager = LinearLayoutManager(this@RoomDetailsActivity)
            adapter = windowAdapter
        }
    }

    private fun setupFloorSpaceRecyclerView() {
        floorSpaceAdapter = FloorSpaceAdapter(floorSpaceList) { selectedFloorSpace ->
            showFloorSpaceOptionsDialog(selectedFloorSpace)
        }

        ui.recyclerFloorSpaces.apply {
            layoutManager = LinearLayoutManager(this@RoomDetailsActivity)
            adapter = floorSpaceAdapter
        }
    }

    private fun loadWindows() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("windows")
            .get()
            .addOnSuccessListener { documents ->
                windowList.clear()

                for (document in documents) {
                    val window = document.toObject(Window::class.java)
                    window.id = document.id
                    windowList.add(window)
                }

                windowAdapter.notifyDataSetChanged()

                if (windowList.isEmpty()) {
                    ui.tvNoWindows.visibility = View.VISIBLE
                    ui.recyclerWindows.visibility = View.GONE
                } else {
                    ui.tvNoWindows.visibility = View.GONE
                    ui.recyclerWindows.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                ui.tvNoWindows.visibility = View.VISIBLE
                ui.recyclerWindows.visibility = View.GONE
                Toast.makeText(this, "Error loading windows: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadFloorSpaces() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("floorSpaces")
            .get()
            .addOnSuccessListener { documents ->
                floorSpaceList.clear()

                for (document in documents) {
                    val floorSpace = document.toObject(FloorSpace::class.java)
                    floorSpace.id = document.id
                    floorSpaceList.add(floorSpace)
                }

                floorSpaceAdapter.notifyDataSetChanged()

                if (floorSpaceList.isEmpty()) {
                    ui.tvNoFloorSpaces.visibility = View.VISIBLE
                    ui.recyclerFloorSpaces.visibility = View.GONE
                } else {
                    ui.tvNoFloorSpaces.visibility = View.GONE
                    ui.recyclerFloorSpaces.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                ui.tvNoFloorSpaces.visibility = View.VISIBLE
                ui.recyclerFloorSpaces.visibility = View.GONE
                Toast.makeText(this, "Error loading floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Room")
            .setMessage("Are you sure you want to delete this room and everything inside it?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRoom()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRoom() {
        val db = Firebase.firestore
        val roomRef = db.collection("houses").document(houseId)
            .collection("rooms")
            .document(roomId)

        roomRef.collection("windows").get()
            .addOnSuccessListener { windowDocs ->
                val windowDeletes = windowDocs.documents.map { doc ->
                    roomRef.collection("windows").document(doc.id).delete()
                }

                Tasks.whenAllComplete(windowDeletes)
                    .addOnSuccessListener {
                        roomRef.collection("floorSpaces").get()
                            .addOnSuccessListener { floorDocs ->
                                val floorDeletes = floorDocs.documents.map { doc ->
                                    roomRef.collection("floorSpaces").document(doc.id).delete()
                                }

                                Tasks.whenAllComplete(floorDeletes)
                                    .addOnSuccessListener {
                                        roomRef.delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Room deleted successfully", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error deleting room: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error deleting floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error loading floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting windows: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading windows: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showWindowOptionsDialog(window: Window) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_item_actions, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val tvDialogMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
        val btnDialogEdit = view.findViewById<Button>(R.id.btnDialogEdit)
        val btnDialogDelete = view.findViewById<Button>(R.id.btnDialogDelete)
        val tvDialogCancel = view.findViewById<TextView>(R.id.tvDialogCancel)

        tvDialogTitle.text = window.name
        tvDialogMessage.text = "Choose an action for this window"

        btnDialogEdit.setOnClickListener {
            dialog.dismiss()
            openEditWindow(window)
        }

        btnDialogDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteWindowConfirmation(window)
        }

        tvDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openEditWindow(window: Window) {
        val intent = Intent(this, AddEditWindowActivity::class.java)
        intent.putExtra("houseId", houseId)
        intent.putExtra("roomId", roomId)
        intent.putExtra("windowId", window.id)
        intent.putExtra("windowName", window.name)
        intent.putExtra("width", window.width.toString())
        intent.putExtra("height", window.height.toString())
        intent.putExtra("selectedProductName", window.productName)
        intent.putExtra("isEdit", true)
        startActivity(intent)
    }

    private fun showDeleteWindowConfirmation(window: Window) {
        AlertDialog.Builder(this)
            .setTitle("Delete Window")
            .setMessage("Are you sure you want to delete ${window.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteWindow(window)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWindow(window: Window) {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("windows")
            .document(window.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Window deleted successfully", Toast.LENGTH_SHORT).show()
                loadWindows()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting window: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showFloorSpaceOptionsDialog(floorSpace: FloorSpace) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_item_actions, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val tvDialogMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
        val btnDialogEdit = view.findViewById<Button>(R.id.btnDialogEdit)
        val btnDialogDelete = view.findViewById<Button>(R.id.btnDialogDelete)
        val tvDialogCancel = view.findViewById<TextView>(R.id.tvDialogCancel)

        tvDialogTitle.text = floorSpace.name
        tvDialogMessage.text = "Choose an action for this floor space"

        btnDialogEdit.setOnClickListener {
            dialog.dismiss()
            openEditFloorSpace(floorSpace)
        }

        btnDialogDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteFloorSpaceConfirmation(floorSpace)
        }

        tvDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openEditFloorSpace(floorSpace: FloorSpace) {
        val intent = Intent(this, AddEditFloorSpaceActivity::class.java)
        intent.putExtra("houseId", houseId)
        intent.putExtra("roomId", roomId)
        intent.putExtra("floorSpaceId", floorSpace.id)
        intent.putExtra("floorSpaceName", floorSpace.name)
        intent.putExtra("width", floorSpace.width.toString())
        intent.putExtra("length", floorSpace.length.toString())
        intent.putExtra("selectedProductName", floorSpace.productName)
        intent.putExtra("isEdit", true)
        startActivity(intent)
    }

    private fun showDeleteFloorSpaceConfirmation(floorSpace: FloorSpace) {
        AlertDialog.Builder(this)
            .setTitle("Delete Floor Space")
            .setMessage("Are you sure you want to delete ${floorSpace.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFloorSpace(floorSpace)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFloorSpace(floorSpace: FloorSpace) {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .collection("floorSpaces")
            .document(floorSpace.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Floor space deleted successfully", Toast.LENGTH_SHORT).show()
                loadFloorSpaces()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting floor space: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
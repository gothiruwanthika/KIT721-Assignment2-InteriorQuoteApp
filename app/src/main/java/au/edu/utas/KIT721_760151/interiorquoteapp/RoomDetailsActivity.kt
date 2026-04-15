package au.edu.utas.KIT721_760151.interiorquoteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityRoomDetailsBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRoomDetailsBinding
    private var houseId: String = ""
    private var roomId: String = ""

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
            Toast.makeText(this, "Edit room clicked", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Delete Room clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadWindows()
        loadFloorSpaces()
    }

    private fun setupWindowRecyclerView() {
        windowAdapter = WindowAdapter(windowList) { selectedWindow ->
            Toast.makeText(this, "Selected window: ${selectedWindow.name}", Toast.LENGTH_SHORT).show()
        }

        ui.recyclerWindows.apply {
            layoutManager = LinearLayoutManager(this@RoomDetailsActivity)
            adapter = windowAdapter
        }
    }

    private fun setupFloorSpaceRecyclerView() {
        floorSpaceAdapter = FloorSpaceAdapter(floorSpaceList) { selectedFloorSpace ->
            Toast.makeText(this, "Selected floor space: ${selectedFloorSpace.name}", Toast.LENGTH_SHORT).show()
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
}
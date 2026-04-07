package au.edu.utas.KIT721_760151.interiorquoteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityHouseDetailsBinding

class HouseDetailsActivity : AppCompatActivity() {

    private lateinit var ui: ActivityHouseDetailsBinding
    private var houseId: String = ""

    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityHouseDetailsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        val customerName = intent.getStringExtra("customerName") ?: ""
        val addressLine1 = intent.getStringExtra("addressLine1") ?: ""
        val city = intent.getStringExtra("city") ?: ""

        ui.tvTitle.text = customerName
        ui.tvCustomerName.text = customerName

        val cityPart = if (city.isNotBlank()) ", $city" else ""
        ui.tvAddress.text = "$addressLine1$cityPart"

        setupRecyclerView()

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnEditHouse.setOnClickListener {
            Toast.makeText(this, "Edit house clicked", Toast.LENGTH_SHORT).show()
        }

        ui.btnAddRoom.setOnClickListener {
            val intent = Intent(this, AddEditRoomActivity::class.java)
            intent.putExtra("houseId", houseId)
            startActivity(intent)
        }

        ui.btnGenerateQuote.setOnClickListener {
            Toast.makeText(this, "Generate Quote clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadRooms()
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(roomList) { selectedRoom ->
            Toast.makeText(this, "Selected room: ${selectedRoom.name}", Toast.LENGTH_SHORT).show()
        }

        ui.recyclerRooms.apply {
            layoutManager = LinearLayoutManager(this@HouseDetailsActivity)
            adapter = roomAdapter
        }
    }

    private fun loadRooms() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms")
            .get()
            .addOnSuccessListener { documents ->
                roomList.clear()

                for (document in documents) {
                    val room = document.toObject(Room::class.java)
                    room.id = document.id
                    roomList.add(room)
                }

                roomAdapter.notifyDataSetChanged()

                if (roomList.isEmpty()) {
                    ui.tvNoRooms.visibility = View.VISIBLE
                    ui.recyclerRooms.visibility = View.GONE
                } else {
                    ui.tvNoRooms.visibility = View.GONE
                    ui.recyclerRooms.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                ui.tvNoRooms.visibility = View.VISIBLE
                ui.recyclerRooms.visibility = View.GONE
                Toast.makeText(this, "Error loading rooms: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
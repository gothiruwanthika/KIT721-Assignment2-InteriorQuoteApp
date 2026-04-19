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

    private var customerName: String = ""
    private var contactNumber: String = ""
    private var addressLine1: String = ""
    private var addressLine2: String = ""
    private var city: String = ""
    private var postalCode: String = ""
    private var email: String = ""

    private lateinit var roomAdapter: RoomAdapter
    private val roomList = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityHouseDetailsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        customerName = intent.getStringExtra("customerName") ?: ""
        contactNumber = intent.getStringExtra("contactNumber") ?: ""
        addressLine1 = intent.getStringExtra("addressLine1") ?: ""
        addressLine2 = intent.getStringExtra("addressLine2") ?: ""
        city = intent.getStringExtra("city") ?: ""
        postalCode = intent.getStringExtra("postalCode") ?: ""
        email = intent.getStringExtra("email") ?: ""

        ui.tvTitle.text = customerName
        ui.tvCustomerName.text = customerName

        val cityPart = if (city.isNotBlank()) ", $city" else ""
        ui.tvAddress.text = "$addressLine1$cityPart"

        setupRecyclerView()

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnEditHouse.setOnClickListener {
            val intent = Intent(this, AddEditHouseActivity::class.java)
            intent.putExtra("houseId", houseId)
            intent.putExtra("customerName", customerName)
            intent.putExtra("contactNumber", contactNumber)
            intent.putExtra("addressLine1", addressLine1)
            intent.putExtra("addressLine2", addressLine2)
            intent.putExtra("city", city)
            intent.putExtra("postalCode", postalCode)
            intent.putExtra("email", email)
            intent.putExtra("isEdit", true)
            startActivity(intent)
        }

        ui.btnAddRoom.setOnClickListener {
            val intent = Intent(this, AddEditRoomActivity::class.java)
            intent.putExtra("houseId", houseId)
            startActivity(intent)
        }

        ui.btnGenerateQuote.setOnClickListener {
            val intent = Intent(this, QuoteActivity::class.java)
            intent.putExtra("houseId", houseId)
            intent.putExtra("customerName", customerName)
            intent.putExtra("addressLine1", addressLine1)
            intent.putExtra("city", city)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadHouseDetails()
        loadRooms()
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(
            rooms = roomList,
            onRoomClick = { selectedRoom ->
                val intent = Intent(this, RoomDetailsActivity::class.java)
                intent.putExtra("houseId", houseId)
                intent.putExtra("roomId", selectedRoom.id)
                intent.putExtra("roomName", selectedRoom.name)
                intent.putExtra("labourCost", selectedRoom.labourCost)
                startActivity(intent)
            },
            onRoomCheckedChanged = { selectedRoom, isChecked ->
                updateRoomIncludedInQuote(selectedRoom, isChecked)
            }
        )

        ui.recyclerRooms.apply {
            layoutManager = LinearLayoutManager(this@HouseDetailsActivity)
            adapter = roomAdapter
        }
    }

    private fun loadHouseDetails() {
        val db = Firebase.firestore

        db.collection("houses")
            .document(houseId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    customerName = document.getString("customerName") ?: ""
                    contactNumber = document.getString("contactNumber") ?: ""
                    addressLine1 = document.getString("addressLine1") ?: ""
                    addressLine2 = document.getString("addressLine2") ?: ""
                    city = document.getString("city") ?: ""
                    postalCode = document.getString("postalCode") ?: ""
                    email = document.getString("email") ?: ""

                    ui.tvTitle.text = customerName
                    ui.tvCustomerName.text = customerName

                    val cityPart = if (city.isNotBlank()) ", $city" else ""
                    ui.tvAddress.text = "$addressLine1$cityPart"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading house details: ${e.message}", Toast.LENGTH_LONG).show()
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

    private fun updateRoomIncludedInQuote(room: Room, isChecked: Boolean) {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms")
            .document(room.id)
            .update("includedInQuote", isChecked)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating room selection: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
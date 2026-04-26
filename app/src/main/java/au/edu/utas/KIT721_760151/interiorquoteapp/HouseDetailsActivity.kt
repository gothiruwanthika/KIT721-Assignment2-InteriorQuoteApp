package au.edu.utas.KIT721_760151.interiorquoteapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityHouseDetailsBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

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
        resetSummary()

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

        ui.btnDeleteHouse.setOnClickListener {
            showDeleteHouseConfirmation()
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
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
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
                resetSummary()

                if (documents.isEmpty) {
                    roomAdapter.notifyDataSetChanged()
                    updateRoomRecyclerHeight()
                    ui.tvNoRooms.visibility = View.VISIBLE
                    ui.recyclerRooms.visibility = View.GONE
                    return@addOnSuccessListener
                }

                val totalRooms = documents.size()
                var processedRooms = 0

                var summaryRoomCount = 0
                var summaryWindowCount = 0
                var summaryFloorCount = 0
                var summaryEstimatedTotal = 0.0

                for (document in documents) {
                    val room = document.toObject(Room::class.java)
                    room.id = document.id

                    db.collection("houses").document(houseId)
                        .collection("rooms").document(room.id)
                        .collection("windows")
                        .get()
                        .addOnSuccessListener { windowDocs ->
                            room.windowCount = windowDocs.size()

                            db.collection("houses").document(houseId)
                                .collection("rooms").document(room.id)
                                .collection("floorSpaces")
                                .get()
                                .addOnSuccessListener { floorDocs ->
                                    room.floorCount = floorDocs.size()

                                    var roomEstimatedTotal = room.labourCost
                                    summaryRoomCount += 1
                                    summaryWindowCount += room.windowCount
                                    summaryFloorCount += room.floorCount

                                    for (windowDoc in windowDocs) {
                                        val window = windowDoc.toObject(Window::class.java)
                                        val pricePerSqm =
                                            if (window.productPricePerSqm > 0) window.productPricePerSqm else 50.0
                                        val areaSqm = (window.width * window.height) / 1_000_000.0
                                        roomEstimatedTotal += areaSqm * pricePerSqm
                                    }

                                    for (floorDoc in floorDocs) {
                                        val floorSpace = floorDoc.toObject(FloorSpace::class.java)
                                        val pricePerSqm =
                                            if (floorSpace.productPricePerSqm > 0) floorSpace.productPricePerSqm else 100.0
                                        val areaSqm = (floorSpace.width * floorSpace.length) / 1_000_000.0
                                        roomEstimatedTotal += areaSqm * pricePerSqm
                                    }

                                    summaryEstimatedTotal += roomEstimatedTotal
                                    roomList.add(room)
                                    processedRooms++

                                    if (processedRooms == totalRooms) {
                                        roomAdapter.notifyDataSetChanged()
                                        updateRoomRecyclerHeight()
                                        updateSummary(
                                            roomCount = summaryRoomCount,
                                            windowCount = summaryWindowCount,
                                            floorCount = summaryFloorCount,
                                            estimatedTotal = summaryEstimatedTotal
                                        )

                                        if (roomList.isEmpty()) {
                                            ui.tvNoRooms.visibility = View.VISIBLE
                                            ui.recyclerRooms.visibility = View.GONE
                                        } else {
                                            ui.tvNoRooms.visibility = View.GONE
                                            ui.recyclerRooms.visibility = View.VISIBLE
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    room.floorCount = 0
                                    summaryRoomCount += 1
                                    summaryWindowCount += room.windowCount
                                    summaryFloorCount += 0
                                    summaryEstimatedTotal += room.labourCost

                                    roomList.add(room)
                                    processedRooms++

                                    if (processedRooms == totalRooms) {
                                        roomAdapter.notifyDataSetChanged()
                                        updateRoomRecyclerHeight()
                                        updateSummary(
                                            roomCount = summaryRoomCount,
                                            windowCount = summaryWindowCount,
                                            floorCount = summaryFloorCount,
                                            estimatedTotal = summaryEstimatedTotal
                                        )

                                        if (roomList.isEmpty()) {
                                            ui.tvNoRooms.visibility = View.VISIBLE
                                            ui.recyclerRooms.visibility = View.GONE
                                        } else {
                                            ui.tvNoRooms.visibility = View.GONE
                                            ui.recyclerRooms.visibility = View.VISIBLE
                                        }
                                    }

                                    Toast.makeText(this, "Error loading floor count: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            room.windowCount = 0
                            room.floorCount = 0
                            summaryRoomCount += 1
                            summaryWindowCount += 0
                            summaryFloorCount += 0
                            summaryEstimatedTotal += room.labourCost

                            roomList.add(room)
                            processedRooms++

                            if (processedRooms == totalRooms) {
                                roomAdapter.notifyDataSetChanged()
                                updateRoomRecyclerHeight()
                                updateSummary(
                                    roomCount = summaryRoomCount,
                                    windowCount = summaryWindowCount,
                                    floorCount = summaryFloorCount,
                                    estimatedTotal = summaryEstimatedTotal
                                )

                                if (roomList.isEmpty()) {
                                    ui.tvNoRooms.visibility = View.VISIBLE
                                    ui.recyclerRooms.visibility = View.GONE
                                } else {
                                    ui.tvNoRooms.visibility = View.GONE
                                    ui.recyclerRooms.visibility = View.VISIBLE
                                }
                            }

                            Toast.makeText(this, "Error loading window count: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                resetSummary()
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

    private fun showDeleteHouseConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete House")
            .setMessage("Are you sure you want to delete this house and all its rooms?")
            .setPositiveButton("Delete") { _, _ ->
                deleteHouseAndRooms()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHouseAndRooms() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms")
            .get()
            .addOnSuccessListener { roomDocuments ->
                val batch = db.batch()

                for (roomDocument in roomDocuments) {
                    batch.delete(roomDocument.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        db.collection("houses").document(houseId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "House deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error deleting house: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error deleting rooms: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error loading rooms for deletion: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun resetSummary() {
        ui.tvSummaryRooms.text = "0"
        ui.tvSummaryWindows.text = "0"
        ui.tvSummaryFloorSpaces.text = "0"
        ui.tvSummaryEstimatedTotal.text = formatPrice(0.0)
    }

    private fun updateSummary(
        roomCount: Int,
        windowCount: Int,
        floorCount: Int,
        estimatedTotal: Double
    ) {
        ui.tvSummaryRooms.text = roomCount.toString()
        ui.tvSummaryWindows.text = windowCount.toString()
        ui.tvSummaryFloorSpaces.text = floorCount.toString()
        ui.tvSummaryEstimatedTotal.text = formatPrice(estimatedTotal)
    }

    private fun updateRoomRecyclerHeight() {
        val itemHeightPx = dpToPx(120f)
        val totalHeight = itemHeightPx * roomList.size
        val params = ui.recyclerRooms.layoutParams
        params.height =
            if (roomList.isEmpty()) ViewGroup.LayoutParams.WRAP_CONTENT else totalHeight
        ui.recyclerRooms.layoutParams = params
        ui.recyclerRooms.requestLayout()
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun formatPrice(value: Double): String {
        return if (value % 1.0 == 0.0) {
            "$${value.toInt()}"
        } else {
            "$" + String.format(Locale.US, "%.2f", value)
        }
    }
}
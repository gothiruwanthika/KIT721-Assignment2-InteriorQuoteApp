package au.edu.utas.KIT721_760151.interiorquoteapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityQuoteBinding
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.QuoteRoomItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class QuoteActivity : AppCompatActivity() {

    private lateinit var ui: ActivityQuoteBinding
    private var houseId: String = ""
    private var quoteShareText: String = ""

    private var customerName: String = ""
    private var customerAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityQuoteBinding.inflate(layoutInflater)
        setContentView(ui.root)

        houseId = intent.getStringExtra("houseId") ?: ""
        customerName = intent.getStringExtra("customerName") ?: ""
        val addressLine1 = intent.getStringExtra("addressLine1") ?: ""
        val city = intent.getStringExtra("city") ?: ""

        val cityPart = if (city.isNotBlank()) ", $city" else ""
        customerAddress = "$addressLine1$cityPart"

        ui.tvCustomerName.text = "Customer: $customerName"
        ui.tvCustomerAddress.text = "Address: $customerAddress"

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnShareQuote.setOnClickListener {
            if (quoteShareText.isBlank()) {
                Toast.makeText(this, "Quote is not ready to share yet", Toast.LENGTH_SHORT).show()
            } else {
                shareQuoteText()
            }
        }

        loadIncludedRooms()
    }

    private fun loadIncludedRooms() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms")
            .whereEqualTo("includedInQuote", true)
            .get()
            .addOnSuccessListener { documents ->
                ui.layoutQuoteRooms.removeAllViews()

                if (documents.isEmpty) {
                    ui.tvTotalQuoteAmount.text = formatPrice(0.0)
                    quoteShareText = buildEmptyQuoteText()
                    return@addOnSuccessListener
                }

                var processedRooms = 0
                var totalQuote = 0.0
                val shareBuilder = StringBuilder()

                shareBuilder.append("Quote\n\n")
                shareBuilder.append("Customer: ").append(customerName).append("\n")
                shareBuilder.append("Address: ").append(customerAddress).append("\n\n")

                for (document in documents) {
                    val room = document.toObject(Room::class.java)
                    room.id = document.id

                    loadRoomQuoteDetails(room) { roomBinding, roomTotal, roomShareText ->
                        ui.layoutQuoteRooms.addView(roomBinding.root)
                        totalQuote += roomTotal
                        processedRooms++

                        shareBuilder.append(roomShareText).append("\n")

                        if (processedRooms == documents.size()) {
                            ui.tvTotalQuoteAmount.text = formatPrice(totalQuote)
                            shareBuilder.append("Total Quote: ").append(formatPrice(totalQuote))
                            quoteShareText = shareBuilder.toString()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading quote: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadRoomQuoteDetails(
        room: Room,
        onComplete: (QuoteRoomItemBinding, Double, String) -> Unit
    ) {
        val db = Firebase.firestore

        val roomBinding = QuoteRoomItemBinding.inflate(
            LayoutInflater.from(this),
            ui.layoutQuoteRooms,
            false
        )

        db.collection("houses").document(houseId)
            .collection("rooms").document(room.id)
            .collection("windows")
            .get()
            .addOnSuccessListener { windowDocs ->

                db.collection("houses").document(houseId)
                    .collection("rooms").document(room.id)
                    .collection("floorSpaces")
                    .get()
                    .addOnSuccessListener { floorDocs ->

                        val windowLinesBuilder = StringBuilder()
                        val floorLinesBuilder = StringBuilder()
                        val roomShareBuilder = StringBuilder()

                        var windowTotal = 0.0
                        var floorTotal = 0.0

                        var windowNumber = 1
                        for (windowDoc in windowDocs) {
                            val window = windowDoc.toObject(Window::class.java)
                            val price = calculateWindowPrice(window.width, window.height)
                            windowTotal += price

                            val windowLine =
                                "Window $windowNumber - ${window.width}mm × ${window.height}mm    ${formatPrice(price)}"

                            windowLinesBuilder.append(windowLine).append("\n")
                            roomShareBuilder.append(windowLine).append("\n")

                            windowNumber++
                        }

                        var floorNumber = 1
                        for (floorDoc in floorDocs) {
                            val floorSpace = floorDoc.toObject(FloorSpace::class.java)
                            val price = calculateFloorPrice(floorSpace.width, floorSpace.length)
                            floorTotal += price

                            val floorLine =
                                "Floor Space $floorNumber - ${floorSpace.width}mm × ${floorSpace.length}mm    ${formatPrice(price)}"

                            floorLinesBuilder.append(floorLine).append("\n")
                            roomShareBuilder.append(floorLine).append("\n")

                            floorNumber++
                        }

                        val windowLines = if (windowLinesBuilder.isBlank()) {
                            "No windows"
                        } else {
                            windowLinesBuilder.toString().trim()
                        }

                        val floorLines = if (floorLinesBuilder.isBlank()) {
                            "No floor spaces"
                        } else {
                            floorLinesBuilder.toString().trim()
                        }

                        val roomTotal = room.labourCost + windowTotal + floorTotal

                        roomBinding.tvRoomName.text = room.name
                        roomBinding.tvWindowLines.text = windowLines
                        roomBinding.tvFloorSpaceLines.text = floorLines
                        roomBinding.tvRoomLabourCost.text = "Labour Cost: ${formatPrice(room.labourCost)}"
                        roomBinding.tvRoomTotal.text = "Room Total: ${formatPrice(roomTotal)}"

                        val roomShareText = buildString {
                            append(room.name).append("\n")
                            append(if (windowLinesBuilder.isBlank()) "No windows" else windowLinesBuilder.toString().trim()).append("\n")
                            append(if (floorLinesBuilder.isBlank()) "No floor spaces" else floorLinesBuilder.toString().trim()).append("\n")
                            append("Labour Cost: ").append(formatPrice(room.labourCost)).append("\n")
                            append("Room Total: ").append(formatPrice(roomTotal)).append("\n")
                        }

                        onComplete(roomBinding, roomTotal, roomShareText)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error loading floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading windows: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun calculateWindowPrice(width: Int, height: Int): Double {
        return (width * height) / 20000.0
    }

    private fun calculateFloorPrice(width: Int, length: Int): Double {
        return (width * length) / 10000.0
    }

    private fun formatPrice(value: Double): String {
        return if (value % 1.0 == 0.0) {
            "$${value.toInt()}"
        } else {
            "$" + String.format(Locale.US, "%.2f", value)
        }
    }

    private fun buildEmptyQuoteText(): String {
        return buildString {
            append("Quote\n\n")
            append("Customer: ").append(customerName).append("\n")
            append("Address: ").append(customerAddress).append("\n\n")
            append("No included rooms\n\n")
            append("Total Quote: ").append(formatPrice(0.0))
        }
    }

    private fun shareQuoteText() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Interior Quote")
            putExtra(Intent.EXTRA_TEXT, quoteShareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    }
}
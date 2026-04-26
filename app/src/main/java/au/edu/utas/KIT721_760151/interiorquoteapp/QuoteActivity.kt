package au.edu.utas.KIT721_760151.interiorquoteapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityQuoteBinding
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.QuoteRoomItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.util.Locale

class QuoteActivity : AppCompatActivity() {

    private lateinit var ui: ActivityQuoteBinding
    private var houseId: String = ""
    private var quoteShareText: String = ""

    private var customerName: String = ""
    private var customerAddress: String = ""

    private val roomQuoteItems = mutableListOf<RoomQuoteItem>()

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

        ui.tvCustomerName.text = customerName
        ui.tvCustomerAddress.text = customerAddress

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnShareQuote.setOnClickListener {
            if (quoteShareText.isBlank()) {
                Toast.makeText(this, "Quote is not ready to share yet", Toast.LENGTH_SHORT).show()
            } else {
                shareQuoteCsvFile()
            }
        }

        loadRoomsForQuote()
    }

    private fun loadRoomsForQuote() {
        val db = Firebase.firestore

        db.collection("houses").document(houseId)
            .collection("rooms")
            .get()
            .addOnSuccessListener { documents ->
                roomQuoteItems.clear()
                ui.layoutQuoteRooms.removeAllViews()

                if (documents.isEmpty) {
                    ui.tvTotalQuoteAmount.text = formatPrice(0.0)
                    quoteShareText = buildEmptyQuoteCsv()
                    return@addOnSuccessListener
                }

                val totalRooms = documents.size()
                var processedRooms = 0

                for (document in documents) {
                    val room = document.toObject(Room::class.java)
                    room.id = document.id

                    loadRoomQuoteDetails(room) { roomQuoteItem ->
                        roomQuoteItems.add(roomQuoteItem)
                        processedRooms++

                        if (processedRooms == totalRooms) {
                            renderQuoteRooms()
                            recalculateQuote()
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
        onComplete: (RoomQuoteItem) -> Unit
    ) {
        val db = Firebase.firestore

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

                        val windowLines = mutableListOf<String>()
                        val floorLines = mutableListOf<String>()
                        var windowTotal = 0.0
                        var floorTotal = 0.0

                        var windowNumber = 1
                        for (windowDoc in windowDocs) {
                            val window = windowDoc.toObject(Window::class.java)

                            val productName =
                                if (window.productName.isBlank()) "No product selected" else window.productName
                            val variantName =
                                if (window.variantName.isBlank()) "Default" else window.variantName
                            val pricePerSqm =
                                if (window.productPricePerSqm > 0) window.productPricePerSqm else 50.0

                            val areaSqm = (window.width * window.height) / 1_000_000.0
                            val price = areaSqm * pricePerSqm
                            windowTotal += price

                            windowLines.add(
                                "Window $windowNumber - $productName - $variantName - ${window.width}mm × ${window.height}mm - ${formatPrice(price)}"
                            )
                            windowNumber++
                        }

                        var floorNumber = 1
                        for (floorDoc in floorDocs) {
                            val floorSpace = floorDoc.toObject(FloorSpace::class.java)

                            val productName =
                                if (floorSpace.productName.isBlank()) "No product selected" else floorSpace.productName
                            val variantName =
                                if (floorSpace.variantName.isBlank()) "Default" else floorSpace.variantName
                            val pricePerSqm =
                                if (floorSpace.productPricePerSqm > 0) floorSpace.productPricePerSqm else 100.0

                            val areaSqm = (floorSpace.width * floorSpace.length) / 1_000_000.0
                            val price = areaSqm * pricePerSqm
                            floorTotal += price

                            floorLines.add(
                                "Floor Space $floorNumber - $productName - $variantName - ${floorSpace.width}mm × ${floorSpace.length}mm - ${formatPrice(price)}"
                            )
                            floorNumber++
                        }

                        val roomTotal = room.labourCost + windowTotal + floorTotal

                        onComplete(
                            RoomQuoteItem(
                                room = room,
                                windowLines = if (windowLines.isEmpty()) listOf("No windows") else windowLines,
                                floorLines = if (floorLines.isEmpty()) listOf("No floor spaces") else floorLines,
                                roomTotal = roomTotal
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error loading floor spaces: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading windows: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun renderQuoteRooms() {
        ui.layoutQuoteRooms.removeAllViews()

        for (roomItem in roomQuoteItems) {
            val roomBinding = QuoteRoomItemBinding.inflate(
                LayoutInflater.from(this),
                ui.layoutQuoteRooms,
                false
            )

            roomBinding.tvRoomName.text = roomItem.room.name
            roomBinding.tvWindowLines.text = roomItem.windowLines.joinToString("\n")
            roomBinding.tvFloorSpaceLines.text = roomItem.floorLines.joinToString("\n")
            roomBinding.tvRoomLabourCost.text = "Labour Cost: ${formatPrice(roomItem.room.labourCost)}"
            roomBinding.tvRoomTotal.text = "Room Total: ${formatPrice(roomItem.roomTotal)}"

            roomBinding.cbIncludeRoom.setOnCheckedChangeListener(null)
            roomBinding.cbIncludeRoom.isChecked = roomItem.room.includedInQuote

            roomBinding.cbIncludeRoom.setOnCheckedChangeListener { _, isChecked ->
                roomItem.room.includedInQuote = isChecked
                updateRoomIncludedInFirestore(roomItem.room.id, isChecked)
                recalculateQuote()
            }

            ui.layoutQuoteRooms.addView(roomBinding.root)
        }
    }

    private fun updateRoomIncludedInFirestore(roomId: String, included: Boolean) {
        val db = Firebase.firestore
        db.collection("houses").document(houseId)
            .collection("rooms").document(roomId)
            .update("includedInQuote", included)
    }

    private fun recalculateQuote() {
        var totalQuote = 0.0

        for (roomItem in roomQuoteItems) {
            if (roomItem.room.includedInQuote) {
                totalQuote += roomItem.roomTotal
            }
        }

        ui.tvTotalQuoteAmount.text = formatPrice(totalQuote)
        quoteShareText = buildQuoteCsv(totalQuote)
    }

    private fun buildQuoteCsv(totalQuote: Double): String {
        val builder = StringBuilder()

        builder.append("Section,Room,Item Type,Item Name,Product,Variant,Measurement,Cost\n")
        builder.append("Customer,,,,$customerName,,,\n")
        builder.append("Address,,,,$customerAddress,,,\n")

        for (roomItem in roomQuoteItems) {
            if (!roomItem.room.includedInQuote) continue

            for (line in roomItem.windowLines) {
                builder.append(csvLineFromDisplayLine(roomItem.room.name, "Window", line)).append("\n")
            }

            for (line in roomItem.floorLines) {
                builder.append(csvLineFromDisplayLine(roomItem.room.name, "Floor Space", line)).append("\n")
            }

            builder.append(
                "Labour,${escapeCsv(roomItem.room.name)},,Labour,,,," +
                        escapeCsv(formatPrice(roomItem.room.labourCost))
            ).append("\n")

            builder.append(
                "Room Total,${escapeCsv(roomItem.room.name)},,,,,," +
                        escapeCsv(formatPrice(roomItem.roomTotal))
            ).append("\n")
        }

        builder.append("Quote Total,,,,,,," + escapeCsv(formatPrice(totalQuote)))

        return builder.toString()
    }

    private fun csvLineFromDisplayLine(roomName: String, itemType: String, line: String): String {
        if (line == "No windows" || line == "No floor spaces") {
            return "$itemType,${escapeCsv(roomName)},${escapeCsv(itemType)},None,None,None,None,None"
        }

        val parts = line.split(" - ")
        val itemName = parts.getOrNull(0) ?: ""
        val product = parts.getOrNull(1) ?: ""
        val variant = parts.getOrNull(2) ?: ""
        val measurement = parts.getOrNull(3) ?: ""
        val cost = parts.getOrNull(4) ?: ""

        return "$itemType,${escapeCsv(roomName)},${escapeCsv(itemType)},${escapeCsv(itemName)},${escapeCsv(product)},${escapeCsv(variant)},${escapeCsv(measurement)},${escapeCsv(cost)}"
    }

    private fun escapeCsv(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    private fun buildEmptyQuoteCsv(): String {
        return buildString {
            append("Section,Room,Item Type,Item Name,Product,Variant,Measurement,Cost\n")
            append("Customer,,,,$customerName,,,\n")
            append("Address,,,,$customerAddress,,,\n")
            append("Quote Total,,,,,,,").append(escapeCsv(formatPrice(0.0)))
        }
    }

    private fun formatPrice(value: Double): String {
        return if (value % 1.0 == 0.0) {
            "$${value.toInt()}"
        } else {
            "$" + String.format(Locale.US, "%.2f", value)
        }
    }

    private fun shareQuoteCsvFile() {
        try {
            val safeCustomerName = customerName
                .replace("[^a-zA-Z0-9-_]".toRegex(), "_")
                .ifBlank { "quote" }

            val csvFile = File(cacheDir, "interior_quote_$safeCustomerName.csv")
            csvFile.writeText(quoteShareText)

            val contentUri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                csvFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Interior Quote CSV")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Quote CSV"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing CSV file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    data class RoomQuoteItem(
        val room: Room,
        val windowLines: List<String>,
        val floorLines: List<String>,
        val roomTotal: Double
    )
}
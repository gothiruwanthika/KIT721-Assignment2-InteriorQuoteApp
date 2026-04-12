package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityRoomDetailsBinding

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRoomDetailsBinding
    private var houseId: String = ""
    private var roomId: String = ""

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

        ui.btnBack.setOnClickListener {
            finish()
        }

        ui.btnEditRoom.setOnClickListener {
            Toast.makeText(this, "Edit room clicked", Toast.LENGTH_SHORT).show()
        }

        ui.btnAddWindow.setOnClickListener {
            Toast.makeText(this, "Add Window clicked", Toast.LENGTH_SHORT).show()
        }

        ui.btnAddFloorSpace.setOnClickListener {
            Toast.makeText(this, "Add Floor Space clicked", Toast.LENGTH_SHORT).show()
        }

        ui.btnDeleteRoom.setOnClickListener {
            Toast.makeText(this, "Delete Room clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
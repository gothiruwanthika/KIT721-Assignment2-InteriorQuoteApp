package au.edu.utas.KIT721_760151.interiorquoteapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityRoomImagePreviewBinding

class RoomImagePreviewActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRoomImagePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityRoomImagePreviewBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val roomName = intent.getStringExtra("roomName") ?: "Room Image"
        val imageBase64 = intent.getStringExtra("imageBase64") ?: ""

        ui.tvTitle.text = roomName

        ui.btnBack.setOnClickListener {
            finish()
        }

        if (imageBase64.isNotBlank()) {
            try {
                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                ui.imgPreview.setImageBitmap(bitmap)
                ui.tvNoImage.visibility = View.GONE
            } catch (e: Exception) {
                ui.tvNoImage.visibility = View.VISIBLE
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        } else {
            ui.tvNoImage.visibility = View.VISIBLE
        }
    }
}
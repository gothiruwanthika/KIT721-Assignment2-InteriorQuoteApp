package au.edu.utas.KIT721_760151.interiorquoteapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnNewHouse.setOnClickListener {
            Toast.makeText(this, "New House clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
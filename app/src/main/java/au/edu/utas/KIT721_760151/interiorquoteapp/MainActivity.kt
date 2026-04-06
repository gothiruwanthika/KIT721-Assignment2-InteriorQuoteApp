package au.edu.utas.KIT721_760151.interiorquoteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var houseAdapter: HouseAdapter
    private val houseList = mutableListOf<House>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        setupRecyclerView()

        ui.btnNewHouse.setOnClickListener {
            val intent = Intent(this, AddEditHouseActivity::class.java)
            startActivity(intent)
        }

        ui.btnAddHouseTop.setOnClickListener {
            val intent = Intent(this, AddEditHouseActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadHouses()
    }

    private fun setupRecyclerView() {
        houseAdapter = HouseAdapter(houseList) { selectedHouse ->
            val intent = Intent(this, HouseDetailsActivity::class.java)
            intent.putExtra("houseId", selectedHouse.id)
            intent.putExtra("customerName", selectedHouse.customerName)
            intent.putExtra("addressLine1", selectedHouse.addressLine1)
            intent.putExtra("city", selectedHouse.city)
            startActivity(intent)
        }

        ui.recyclerHouses.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = houseAdapter
        }
    }

    private fun loadHouses() {
        val db = Firebase.firestore

        db.collection("houses")
            .get()
            .addOnSuccessListener { documents ->
                houseList.clear()

                for (document in documents) {
                    val house = document.toObject(House::class.java)
                    house.id = document.id
                    houseList.add(house)
                }

                houseAdapter.notifyDataSetChanged()

                if (houseList.isEmpty()) {
                    showEmptyState()
                } else {
                    showHouseList()
                }
            }
            .addOnFailureListener {
                showEmptyState()
            }
    }

    private fun showEmptyState() {
        ui.layoutEmptyState.visibility = View.VISIBLE
        ui.recyclerHouses.visibility = View.GONE
        ui.btnAddHouseTop.visibility = View.GONE
    }

    private fun showHouseList() {
        ui.layoutEmptyState.visibility = View.GONE
        ui.recyclerHouses.visibility = View.VISIBLE
        ui.btnAddHouseTop.visibility = View.VISIBLE
    }
}
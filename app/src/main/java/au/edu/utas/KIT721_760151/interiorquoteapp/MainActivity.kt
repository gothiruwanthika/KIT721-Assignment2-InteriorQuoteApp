package au.edu.utas.KIT721_760151.interiorquoteapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var houseAdapter: HouseAdapter

    private val allHouseList = mutableListOf<House>()
    private val filteredHouseList = mutableListOf<House>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        setupRecyclerView()
        setupSearch()

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
        houseAdapter = HouseAdapter(filteredHouseList) { selectedHouse ->
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

    private fun setupSearch() {
        ui.etSearchHouse.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterHouses(s?.toString().orEmpty())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadHouses() {
        val db = Firebase.firestore

        db.collection("houses")
            .get()
            .addOnSuccessListener { documents ->
                allHouseList.clear()
                filteredHouseList.clear()

                if (documents.isEmpty) {
                    houseAdapter.notifyDataSetChanged()
                    showEmptyState()
                    return@addOnSuccessListener
                }

                val totalHouses = documents.size()
                var processedHouses = 0

                for (document in documents) {
                    val house = document.toObject(House::class.java)
                    house.id = document.id

                    db.collection("houses")
                        .document(house.id)
                        .collection("rooms")
                        .get()
                        .addOnSuccessListener { roomDocs ->
                            house.roomCount = roomDocs.size()
                            allHouseList.add(house)
                            processedHouses++

                            if (processedHouses == totalHouses) {
                                filterHouses(ui.etSearchHouse.text.toString())
                            }
                        }
                        .addOnFailureListener {
                            house.roomCount = 0
                            allHouseList.add(house)
                            processedHouses++

                            if (processedHouses == totalHouses) {
                                filterHouses(ui.etSearchHouse.text.toString())
                            }
                        }
                }
            }
            .addOnFailureListener {
                showEmptyState()
            }
    }

    private fun filterHouses(query: String) {
        filteredHouseList.clear()

        val searchText = query.trim().lowercase()

        if (searchText.isBlank()) {
            filteredHouseList.addAll(allHouseList)
        } else {
            for (house in allHouseList) {
                val matchesCustomer = house.customerName.lowercase().contains(searchText)
                val matchesAddress = house.addressLine1.lowercase().contains(searchText)
                val matchesCity = house.city.lowercase().contains(searchText)

                if (matchesCustomer || matchesAddress || matchesCity) {
                    filteredHouseList.add(house)
                }
            }
        }

        houseAdapter.notifyDataSetChanged()

        if (allHouseList.isEmpty()) {
            showEmptyState()
        } else {
            showHouseList()
        }
    }

    private fun showEmptyState() {
        ui.layoutEmptyState.visibility = View.VISIBLE
        ui.recyclerHouses.visibility = View.GONE
        ui.btnAddHouseTop.visibility = View.GONE
        ui.etSearchHouse.visibility = View.GONE
    }

    private fun showHouseList() {
        ui.layoutEmptyState.visibility = View.GONE
        ui.recyclerHouses.visibility = View.VISIBLE
        ui.btnAddHouseTop.visibility = View.VISIBLE
        ui.etSearchHouse.visibility = View.VISIBLE
    }
}
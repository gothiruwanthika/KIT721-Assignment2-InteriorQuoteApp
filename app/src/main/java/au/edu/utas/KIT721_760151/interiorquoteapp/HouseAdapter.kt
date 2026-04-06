package au.edu.utas.KIT721_760151.interiorquoteapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.HouseItemBinding

class HouseAdapter(
    private val houses: MutableList<House>,
    private val onHouseClick: (House) -> Unit
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    inner class HouseViewHolder(val ui: HouseItemBinding) : RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val ui = HouseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HouseViewHolder(ui)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]

        holder.ui.tvCustomerName.text = house.customerName

        val cityPart = if (house.city.isNotBlank()) ", ${house.city}" else ""
        holder.ui.tvHouseDetails.text = "${house.addressLine1}$cityPart | no rooms"

        holder.ui.root.setOnClickListener {
            onHouseClick(house)
        }
    }

    override fun getItemCount(): Int {
        return houses.size
    }
}
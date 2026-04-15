package au.edu.utas.KIT721_760151.interiorquoteapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.FloorSpaceItemBinding

class FloorSpaceAdapter(
    private val floorSpaces: MutableList<FloorSpace>,
    private val onFloorSpaceClick: (FloorSpace) -> Unit
) : RecyclerView.Adapter<FloorSpaceAdapter.FloorSpaceViewHolder>() {

    inner class FloorSpaceViewHolder(val ui: FloorSpaceItemBinding) :
        RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorSpaceViewHolder {
        val ui = FloorSpaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FloorSpaceViewHolder(ui)
    }

    override fun onBindViewHolder(holder: FloorSpaceViewHolder, position: Int) {
        val floorSpace = floorSpaces[position]

        holder.ui.tvFloorSpaceName.text = floorSpace.name
        holder.ui.tvFloorSpaceDetails.text =
            "Width: ${floorSpace.width} mm | Length: ${floorSpace.length} mm"

        holder.ui.root.setOnClickListener {
            onFloorSpaceClick(floorSpace)
        }
    }

    override fun getItemCount(): Int {
        return floorSpaces.size
    }
}
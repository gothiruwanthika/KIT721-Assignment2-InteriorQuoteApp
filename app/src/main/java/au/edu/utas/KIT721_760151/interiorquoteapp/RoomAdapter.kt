package au.edu.utas.KIT721_760151.interiorquoteapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.RoomItemBinding

class RoomAdapter(
    private val rooms: MutableList<Room>,
    private val onRoomClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(val ui: RoomItemBinding) : RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val ui = RoomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(ui)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]

        holder.ui.tvRoomName.text = room.name
        holder.ui.tvRoomDetails.text = "Labour Cost: $${room.labourCost} | no windows | no floors"

        holder.ui.root.setOnClickListener {
            onRoomClick(room)
        }
    }

    override fun getItemCount(): Int {
        return rooms.size
    }
}
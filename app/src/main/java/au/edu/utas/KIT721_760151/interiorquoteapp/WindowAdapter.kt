package au.edu.utas.KIT721_760151.interiorquoteapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.WindowItemBinding

class WindowAdapter(
    private val windows: MutableList<Window>,
    private val onWindowClick: (Window) -> Unit
) : RecyclerView.Adapter<WindowAdapter.WindowViewHolder>() {

    inner class WindowViewHolder(val ui: WindowItemBinding) : RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WindowViewHolder {
        val ui = WindowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WindowViewHolder(ui)
    }

    override fun onBindViewHolder(holder: WindowViewHolder, position: Int) {
        val window = windows[position]

        holder.ui.tvWindowName.text = window.name
        holder.ui.tvWindowDetails.text = "Width: ${window.width} mm | Height: ${window.height} mm"

        holder.ui.root.setOnClickListener {
            onWindowClick(window)
        }
    }

    override fun getItemCount(): Int {
        return windows.size
    }
}
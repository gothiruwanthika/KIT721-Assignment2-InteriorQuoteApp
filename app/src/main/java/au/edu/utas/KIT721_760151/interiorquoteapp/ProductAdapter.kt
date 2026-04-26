package au.edu.utas.KIT721_760151.interiorquoteapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ProductItemBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var selectedProductId: String? = null

    inner class ProductViewHolder(val ui: ProductItemBinding) : RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val ui = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(ui)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.ui.tvProductName.text = product.name
        holder.ui.tvProductDescription.text = product.description
        holder.ui.tvProductPrice.text = "$${product.pricePerSqm} / m²"
        holder.ui.imgProduct.setImageResource(getProductImage(product.name))

        val isSelected = product.id == selectedProductId
        holder.ui.root.setCardBackgroundColor(
            Color.parseColor(if (isSelected) "#EEE8FF" else "#FFFFFF")
        )
        holder.ui.root.cardElevation = if (isSelected) 8f else 4f

        holder.ui.root.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun setSelectedProduct(productId: String?) {
        selectedProductId = productId
        notifyDataSetChanged()
    }

    private fun getProductImage(productName: String): Int {
        return when (productName.lowercase()) {
            "standard roller blind" -> R.drawable.roller_blind
            "modular vertical slat" -> R.drawable.venetian_blind
            "fixed-width plantation shutter" -> R.drawable.shutter
            "extra wide sheer curtain" -> R.drawable.curtain
            "thermal blackout blind" -> R.drawable.roller_blind
            "cafe-style half shutter" -> R.drawable.shutter
            "slimline aluminium venetian" -> R.drawable.venetian_blind
            "velvet theater drape" -> R.drawable.curtain
            "bamboo eco-roll" -> R.drawable.roller_blind
            "industrial skylight blind" -> R.drawable.roller_blind
            "premium wool carpet" -> R.drawable.vinyl_flooring
            "commercial grade nylon" -> R.drawable.laminate_flooring
            "engineered oak floorboards" -> R.drawable.timber_flooring
            "recycled rubber gym floor" -> R.drawable.tiles
            "luxury vinyl plank" -> R.drawable.vinyl_flooring
            "polished concrete tiles" -> R.drawable.tiles
            "berber loop special" -> R.drawable.laminate_flooring
            "eco-cork floating floor" -> R.drawable.timber_flooring
            "parquetry herringbone oak" -> R.drawable.timber_flooring
            "pet-proof synthetic turf" -> R.drawable.vinyl_flooring
            else -> android.R.drawable.ic_menu_gallery
        }
    }
}
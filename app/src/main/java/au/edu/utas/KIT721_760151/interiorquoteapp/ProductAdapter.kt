package au.edu.utas.KIT721_760151.interiorquoteapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.KIT721_760151.interiorquoteapp.databinding.ProductItemBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val ui: ProductItemBinding) : RecyclerView.ViewHolder(ui.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val ui = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(ui)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.ui.tvProductName.text = product.name
        holder.ui.tvProductDescription.text = product.description
        holder.ui.tvProductPrice.text = product.priceText

        holder.ui.root.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size
}
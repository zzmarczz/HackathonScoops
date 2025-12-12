package com.icecream.demo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.icecream.demo.api.IceCreamApiService
import com.icecream.demo.model.IceCream
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton


/**
 * Main Activity displaying 6 ice cream flavors in a grid.
 */
class IceCreamMainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartFab: ExtendedFloatingActionButton
    private lateinit var adapter: IceCreamAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: View

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_icecream_main)
        
        setupToolbar()
        setupViews()
        setupCartFab()
        observeCart()
        
        // Fetch menu from API
        fetchMenuFromApi()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewFlavors)
        progressBar = findViewById(R.id.progressBar)
        
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = IceCreamAdapter(emptyList()) { iceCream ->
            onIceCreamSelected(iceCream)
        }
        recyclerView.adapter = adapter
    }

    private fun fetchMenuFromApi() {
        showLoading(true)
        
        IceCreamApiService.fetchMenu { result ->
            runOnUiThread {
                showLoading(false)
                
                result.onSuccess { flavors ->
                    adapter.updateFlavors(flavors)
                    // Also check inventory for these items
                    checkInventoryForItems(flavors)
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        "Failed to load menu: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    // Fallback to local data
                    adapter.updateFlavors(IceCream.getSampleFlavors())
                }
            }
        }
    }

    private fun checkInventoryForItems(flavors: List<IceCream>) {
        val ids = flavors.map { it.id }
        IceCreamApiService.checkInventory(ids) { result ->
            runOnUiThread {
                result.onSuccess { inventory ->
                    android.util.Log.d("IceCreamShop", "Inventory check: $inventory")
                }.onFailure { error ->
                    android.util.Log.e("IceCreamShop", "Inventory check failed: ${error.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "🍦 Scoops & Smiles"
            setDisplayHomeAsUpEnabled(false)
        }
    }


    private fun setupCartFab() {
        cartFab = findViewById(R.id.fabCart)
        cartFab.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun observeCart() {
        CartManager.cartCount.observe(this) { count ->
            updateCartBadge(count)
        }
    }

    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            cartFab.text = "Cart ($count)"
            cartFab.extend()
        } else {
            cartFab.text = "Cart"
            cartFab.shrink()
        }
    }

    private fun onIceCreamSelected(iceCream: IceCream) {
        CartManager.addToCart(iceCream)
        Toast.makeText(this, "${iceCream.name} added to cart! 🍨", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_icecream_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                startActivity(Intent(this, CartActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * RecyclerView Adapter for displaying ice cream flavors
     */
    inner class IceCreamAdapter(
        private var flavors: List<IceCream>,
        private val onItemClick: (IceCream) -> Unit
    ) : RecyclerView.Adapter<IceCreamAdapter.ViewHolder>() {

        fun updateFlavors(newFlavors: List<IceCream>) {
            flavors = newFlavors
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: CardView = view.findViewById(R.id.cardIceCream)
            val colorView: View = view.findViewById(R.id.viewFlavorColor)
            val icon: ImageView = view.findViewById(R.id.imgIceCream)
            val name: TextView = view.findViewById(R.id.txtFlavorName)
            val description: TextView = view.findViewById(R.id.txtFlavorDescription)
            val price: TextView = view.findViewById(R.id.txtPrice)
            val addButton: MaterialButton = view.findViewById(R.id.btnAddToCart)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_icecream, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iceCream = flavors[position]
            
            holder.name.text = iceCream.name
            holder.description.text = iceCream.description
            holder.price.text = "$${String.format("%.2f", iceCream.price)}"
            
            // Set flavor color
            try {
                holder.colorView.setBackgroundColor(Color.parseColor(iceCream.colorHex))
            } catch (e: Exception) {
                holder.colorView.setBackgroundColor(Color.LTGRAY)
            }
            
            // Set ice cream icon based on flavor
            val iconRes = when (iceCream.id) {
                1 -> R.drawable.ic_icecream_vanilla
                2 -> R.drawable.ic_icecream_chocolate
                3 -> R.drawable.ic_icecream_strawberry
                4 -> R.drawable.ic_icecream_mint
                5 -> R.drawable.ic_icecream_caramel
                6 -> R.drawable.ic_icecream_double_chocolate
                else -> R.drawable.ic_icecream_default
            }
            holder.icon.setImageResource(iconRes)
            
            holder.addButton.setOnClickListener {
                onItemClick(iceCream)
            }
            
            holder.card.setOnClickListener {
                onItemClick(iceCream)
            }
        }

        override fun getItemCount() = flavors.size
    }
}


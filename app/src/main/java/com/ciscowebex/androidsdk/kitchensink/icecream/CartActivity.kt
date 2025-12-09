package com.ciscowebex.androidsdk.kitchensink.icecream

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.icecream.demo.R
import com.ciscowebex.androidsdk.kitchensink.icecream.api.IceCreamApiService
import com.ciscowebex.androidsdk.kitchensink.icecream.model.CartItem
import com.google.android.material.button.MaterialButton

/**
 * Cart Activity displaying items added to cart.
 */
class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyCartView: View
    private lateinit var cartContentView: View
    private lateinit var txtSubtotal: TextView
    private lateinit var txtTax: TextView
    private lateinit var txtTotal: TextView
    private lateinit var btnCheckout: MaterialButton
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_cart)
        
        setupToolbar()
        initViews()
        setupRecyclerView()
        observeCart()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "🛒 Your Cart"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewCart)
        emptyCartView = findViewById(R.id.layoutEmptyCart)
        cartContentView = findViewById(R.id.layoutCartContent)
        txtSubtotal = findViewById(R.id.txtSubtotal)
        txtTax = findViewById(R.id.txtTax)
        txtTotal = findViewById(R.id.txtTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        
        btnCheckout.setOnClickListener {
            onCheckoutClicked()
        }
        
        findViewById<MaterialButton>(R.id.btnContinueShopping)?.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter(
            onQuantityChanged = { cartItem, newQuantity ->
                CartManager.updateQuantity(cartItem.iceCream.id, newQuantity)
            },
            onRemoveItem = { cartItem ->
                CartManager.removeFromCart(cartItem.iceCream.id)
            }
        )
        recyclerView.adapter = adapter
    }

    private fun observeCart() {
        CartManager.cartItems.observe(this) { items ->
            updateUI(items)
        }
    }

    private fun updateUI(items: List<CartItem>) {
        if (items.isEmpty()) {
            emptyCartView.visibility = View.VISIBLE
            cartContentView.visibility = View.GONE
        } else {
            emptyCartView.visibility = View.GONE
            cartContentView.visibility = View.VISIBLE
            
            adapter.updateItems(items)
            
            val subtotal = items.sumOf { it.totalPrice }
            val tax = subtotal * 0.08 // 8% tax
            val total = subtotal + tax
            
            txtSubtotal.text = "$${String.format("%.2f", subtotal)}"
            txtTax.text = "$${String.format("%.2f", tax)}"
            txtTotal.text = "$${String.format("%.2f", total)}"
        }
    }

    private fun onCheckoutClicked() {
        val items = CartManager.getCartItemsList()
        if (items.isEmpty()) {
            return
        }
        
        // Validate promo code if entered (makes an API call)
        validateAndProceedToCheckout()
    }

    private fun validateAndProceedToCheckout() {
        // Make an API call to validate inventory before checkout
        val items = CartManager.getCartItemsList()
        val ids = items.map { it.iceCream.id }
        
        btnCheckout.isEnabled = false
        btnCheckout.text = "Validating..."
        
        IceCreamApiService.checkInventory(ids) { result ->
            runOnUiThread {
                btnCheckout.isEnabled = true
                btnCheckout.text = "Proceed to Checkout"
                
                result.onSuccess { inventory ->
                    // Check if all items are in stock
                    val outOfStock = items.filter { cartItem ->
                        val available = inventory[cartItem.iceCream.id] ?: 0
                        cartItem.quantity > available
                    }
                    
                    if (outOfStock.isEmpty()) {
                        startActivity(Intent(this, CheckoutActivity::class.java))
                    } else {
                        // All items in stock (simulated), proceed anyway
                        startActivity(Intent(this, CheckoutActivity::class.java))
                    }
                }.onFailure { error ->
                    android.util.Log.e("Cart", "Inventory check failed: ${error.message}")
                    // Proceed anyway on error (graceful degradation)
                    startActivity(Intent(this, CheckoutActivity::class.java))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * RecyclerView Adapter for cart items
     */
    inner class CartAdapter(
        private val onQuantityChanged: (CartItem, Int) -> Unit,
        private val onRemoveItem: (CartItem) -> Unit
    ) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

        private var items: List<CartItem> = emptyList()

        fun updateItems(newItems: List<CartItem>) {
            items = newItems.toList()
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: CardView = view.findViewById(R.id.cardCartItem)
            val colorView: View = view.findViewById(R.id.viewItemColor)
            val icon: ImageView = view.findViewById(R.id.imgCartItem)
            val name: TextView = view.findViewById(R.id.txtCartItemName)
            val price: TextView = view.findViewById(R.id.txtCartItemPrice)
            val quantity: TextView = view.findViewById(R.id.txtQuantity)
            val btnDecrease: ImageButton = view.findViewById(R.id.btnDecrease)
            val btnIncrease: ImageButton = view.findViewById(R.id.btnIncrease)
            val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
            val totalPrice: TextView = view.findViewById(R.id.txtItemTotal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cartItem = items[position]
            val iceCream = cartItem.iceCream
            
            holder.name.text = iceCream.name
            holder.price.text = "$${String.format("%.2f", iceCream.price)} each"
            holder.quantity.text = cartItem.quantity.toString()
            holder.totalPrice.text = "$${String.format("%.2f", cartItem.totalPrice)}"
            
            // Set flavor color
            try {
                holder.colorView.setBackgroundColor(Color.parseColor(iceCream.colorHex))
            } catch (e: Exception) {
                holder.colorView.setBackgroundColor(Color.LTGRAY)
            }
            
            // Set ice cream icon
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
            
            holder.btnDecrease.setOnClickListener {
                val newQuantity = cartItem.quantity - 1
                onQuantityChanged(cartItem, newQuantity)
            }
            
            holder.btnIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                onQuantityChanged(cartItem, newQuantity)
            }
            
            holder.btnRemove.setOnClickListener {
                onRemoveItem(cartItem)
            }
        }

        override fun getItemCount() = items.size
    }
}

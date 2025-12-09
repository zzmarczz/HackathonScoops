package com.ciscowebex.androidsdk.kitchensink.icecream

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.icecream.model.CartItem
import com.ciscowebex.androidsdk.kitchensink.icecream.model.IceCream

/**
 * Singleton CartManager to manage shopping cart state.
 */
object CartManager {
    
    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems
    
    private val _cartCount = MutableLiveData(0)
    val cartCount: LiveData<Int> = _cartCount
    
    private val _cartTotal = MutableLiveData(0.0)
    val cartTotal: LiveData<Double> = _cartTotal
    
    /**
     * Add an ice cream to the cart.
     */
    fun addToCart(iceCream: IceCream) {
        val currentList = _cartItems.value ?: mutableListOf()
        val existingItem = currentList.find { it.iceCream.id == iceCream.id }
        
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            currentList.add(CartItem(iceCream, 1))
        }
        
        _cartItems.value = currentList
        updateCounts()
    }
    
    /**
     * Remove an item from the cart.
     */
    fun removeFromCart(iceCreamId: Int) {
        val currentList = _cartItems.value ?: mutableListOf()
        currentList.removeAll { it.iceCream.id == iceCreamId }
        _cartItems.value = currentList
        updateCounts()
    }
    
    /**
     * Update quantity of an item in the cart.
     */
    fun updateQuantity(iceCreamId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(iceCreamId)
            return
        }
        
        val currentList = _cartItems.value ?: mutableListOf()
        currentList.find { it.iceCream.id == iceCreamId }?.quantity = quantity
        _cartItems.value = currentList
        updateCounts()
    }
    
    /**
     * Clear all items from the cart.
     */
    fun clearCart() {
        _cartItems.value = mutableListOf()
        updateCounts()
    }
    
    /**
     * Get the current cart items list.
     */
    fun getCartItemsList(): List<CartItem> {
        return _cartItems.value?.toList() ?: emptyList()
    }
    
    private fun updateCounts() {
        val items = _cartItems.value ?: mutableListOf()
        _cartCount.value = items.sumOf { it.quantity }
        _cartTotal.value = items.sumOf { it.totalPrice }
    }
}

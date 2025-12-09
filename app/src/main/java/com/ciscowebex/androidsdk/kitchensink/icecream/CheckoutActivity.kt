package com.ciscowebex.androidsdk.kitchensink.icecream

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.icecream.demo.R
import com.ciscowebex.androidsdk.kitchensink.icecream.api.IceCreamApiService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.appdynamics.eumagent.runtime.Instrumentation

/**
 * Checkout Activity for completing the order.
 */
class CheckoutActivity : AppCompatActivity() {

    private lateinit var txtOrderSummary: TextView
    private lateinit var txtTotal: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var btnPlaceOrder: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutCheckoutForm: View
    private lateinit var layoutOrderSuccess: View
    private lateinit var txtOrderNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_checkout)
        
        setupToolbar()
        initViews()
        displayOrderSummary()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "💳 Checkout"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initViews() {
        txtOrderSummary = findViewById(R.id.txtOrderSummary)
        txtTotal = findViewById(R.id.txtCheckoutTotal)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etAddress = findViewById(R.id.etAddress)
        etCardNumber = findViewById(R.id.etCardNumber)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        progressBar = findViewById(R.id.progressCheckout)
        layoutCheckoutForm = findViewById(R.id.layoutCheckoutForm)
        layoutOrderSuccess = findViewById(R.id.layoutOrderSuccess)
        txtOrderNumber = findViewById(R.id.txtOrderNumber)
        
        btnPlaceOrder.setOnClickListener {
            processOrder()
        }
        
        findViewById<MaterialButton>(R.id.btnBackToShop)?.setOnClickListener {
            navigateBackToShop()
        }
    }

    private fun displayOrderSummary() {
        val items = CartManager.getCartItemsList()
        val subtotal = items.sumOf { it.totalPrice }
        val tax = subtotal * 0.08
        val total = subtotal + tax
        
        val summary = StringBuilder()
        items.forEach { item ->
            summary.append("${item.quantity}x ${item.iceCream.name} - $${String.format("%.2f", item.totalPrice)}\n")
        }
        summary.append("\nSubtotal: $${String.format("%.2f", subtotal)}")
        summary.append("\nTax (8%): $${String.format("%.2f", tax)}")
        
        txtOrderSummary.text = summary.toString()
        txtTotal.text = "Total: $${String.format("%.2f", total)}"
    }

    private fun processOrder() {
        // Validate inputs
        if (!validateInputs()) {
            return
        }
        
        // Show loading state
        showLoading(true)
        
        // Submit order via real API call
        val items = CartManager.getCartItemsList()
        val customerName = etName.text.toString()
        val customerEmail = etEmail.text.toString()
        val shippingAddress = etAddress.text.toString()

        Instrumentation.setUserData("CustomerName", customerName)



        // Report total ice cream count as a metric
        val totalIceCreams = items.sumOf { it.quantity }
        Instrumentation.reportMetric("Ice Cream Order Count", totalIceCreams.toLong())


        IceCreamApiService.submitOrder(
            items = items,
            customerName = customerName,
            customerEmail = customerEmail,
            shippingAddress = shippingAddress
        ) { result ->
            runOnUiThread {
                showLoading(false)
                
                result.onSuccess { response ->
                    if (response.success) {
                        onOrderSuccess(response.orderId, response.estimatedDelivery)
                    } else {
                        onOrderFailed()
                    }
                }.onFailure { error ->
                    android.util.Log.e("Checkout", "Order failed: ${error.message}")
                    onOrderFailed()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etName.text.isNullOrBlank()) {
            etName.error = "Name is required"
            isValid = false
        }
        
        if (etEmail.text.isNullOrBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
            etEmail.error = "Valid email is required"
            isValid = false
        }
        
        if (etAddress.text.isNullOrBlank()) {
            etAddress.error = "Address is required"
            isValid = false
        }
        
        if (etCardNumber.text.isNullOrBlank() || etCardNumber.text.toString().length < 16) {
            etCardNumber.error = "Valid card number is required"
            isValid = false
        }
        
        return isValid
    }

    private fun onOrderSuccess(orderId: String, estimatedDelivery: String = "15-20 minutes") {
        // Clear the cart
        CartManager.clearCart()
        
        // Show success UI
        layoutCheckoutForm.visibility = View.GONE
        layoutOrderSuccess.visibility = View.VISIBLE
        txtOrderNumber.text = "Order #$orderId"
        
        supportActionBar?.title = "✅ Order Confirmed"
    }

    private fun onOrderFailed() {
        // Show error to user
        btnPlaceOrder.text = "Retry Order"
        com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content),
            "Order failed. Please try again.",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnPlaceOrder.isEnabled = !show
        btnPlaceOrder.text = if (show) "Processing..." else "Place Order"
    }

    private fun navigateBackToShop() {
        // Navigate back to main screen and clear the backstack
        val intent = Intent(this, IceCreamMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

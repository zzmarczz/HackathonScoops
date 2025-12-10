package com.icecream.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.icecream.demo.model.IceCream
import com.google.android.material.button.MaterialButton
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * Simulates user sessions with realistic user journeys through the ice cream shop.
 */
object SessionSimulator {

    private const val TAG = "SessionSimulator"
    
    // Simulation timing (in milliseconds)
    private const val SHORT_DELAY = 500L
    private const val MEDIUM_DELAY = 1000L
    private const val LONG_DELAY = 2000L
    private const val TYPING_DELAY = 100L
    
    private val isRunning = AtomicBoolean(false)
    private val handler = Handler(Looper.getMainLooper())
    
    // Sample customer data for simulations
    private val sampleNames = listOf(
        "John Smith", "Emma Wilson", "Michael Brown", "Sarah Davis",
        "James Johnson", "Emily Taylor", "David Martinez", "Jessica Anderson"
    )
    
    private val sampleEmails = listOf(
        "john@example.com", "emma@test.com", "mike@demo.com", "sarah@sample.com",
        "james@email.com", "emily@mail.com", "david@test.org", "jessica@demo.net"
    )
    
    private val sampleAddresses = listOf(
        "123 Main St, New York, NY 10001",
        "456 Oak Ave, Los Angeles, CA 90001",
        "789 Pine Rd, Chicago, IL 60601",
        "321 Elm Blvd, Houston, TX 77001",
        "654 Maple Dr, Phoenix, AZ 85001"
    )

    /**
     * Start a single simulated session.
     * Simulates: Browse -> Add to Cart -> View Cart -> Checkout -> Complete
     */
    fun startSession(activity: Activity, onComplete: (() -> Unit)? = null) {
        if (isRunning.get()) {
            Log.w(TAG, "Session already running")
            return
        }
        
        isRunning.set(true)
        Log.i(TAG, "Starting simulated session...")
        
        // Clear any existing cart
        CartManager.clearCart()
        
        // Start the simulation sequence
        simulateBrowsingPhase(activity) {
            simulateAddToCartPhase(activity) {
                simulateCartViewPhase(activity) {
                    simulateCheckoutPhase(activity) {
                        Log.i(TAG, "Session completed!")
                        isRunning.set(false)
                        onComplete?.invoke()
                    }
                }
            }
        }
    }

    /**
     * Start multiple sessions with delays between them.
     */
    fun startMultipleSessions(
        activity: Activity,
        count: Int,
        delayBetweenSessions: Long = 5000L,
        onAllComplete: (() -> Unit)? = null
    ) {
        var completedCount = 0
        
        fun runNextSession() {
            if (completedCount >= count) {
                Log.i(TAG, "All $count sessions completed!")
                onAllComplete?.invoke()
                return
            }
            
            Log.i(TAG, "Starting session ${completedCount + 1} of $count")
            
            startSession(activity) {
                completedCount++
                
                if (completedCount < count) {
                    // Wait before starting next session
                    handler.postDelayed({
                        runNextSession()
                    }, delayBetweenSessions)
                } else {
                    onAllComplete?.invoke()
                }
            }
        }
        
        runNextSession()
    }

    /**
     * Stop any running simulation.
     */
    fun stop() {
        isRunning.set(false)
        handler.removeCallbacksAndMessages(null)
        Log.i(TAG, "Simulation stopped")
    }

    /**
     * Phase 1: Simulate browsing the menu
     */
    private fun simulateBrowsingPhase(activity: Activity, onComplete: () -> Unit) {
        Log.d(TAG, "Phase 1: Browsing menu...")
        
        // Wait for menu to load, then scroll around
        handler.postDelayed({
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerViewFlavors)
            
            // Simulate scrolling through the menu
            recyclerView?.let { rv ->
                // Scroll down
                handler.postDelayed({
                    rv.smoothScrollToPosition(3)
                }, SHORT_DELAY)
                
                // Scroll back up
                handler.postDelayed({
                    rv.smoothScrollToPosition(0)
                }, MEDIUM_DELAY)
            }
            
            handler.postDelayed({
                onComplete()
            }, LONG_DELAY)
            
        }, MEDIUM_DELAY)
    }

    /**
     * Phase 2: Add random items to cart
     */
    private fun simulateAddToCartPhase(activity: Activity, onComplete: () -> Unit) {
        Log.d(TAG, "Phase 2: Adding items to cart...")
        
        // Get random flavors to add
        val flavors = IceCream.getSampleFlavors()
        val itemsToAdd = (1..Random.nextInt(2, 4)).map {
            flavors.random()
        }
        
        var addedCount = 0
        
        fun addNextItem() {
            if (addedCount >= itemsToAdd.size) {
                handler.postDelayed({
                    onComplete()
                }, SHORT_DELAY)
                return
            }
            
            val iceCream = itemsToAdd[addedCount]
            Log.d(TAG, "Adding to cart: ${iceCream.name}")
            
            // Add to cart (this triggers UI updates)
            CartManager.addToCart(iceCream)
            addedCount++
            
            // Small delay between adds
            handler.postDelayed({
                addNextItem()
            }, MEDIUM_DELAY)
        }
        
        handler.postDelayed({
            addNextItem()
        }, SHORT_DELAY)
    }

    /**
     * Phase 3: View the cart
     */
    private fun simulateCartViewPhase(activity: Activity, onComplete: () -> Unit) {
        Log.d(TAG, "Phase 3: Viewing cart...")
        
        // Navigate to cart
        handler.postDelayed({
            val intent = Intent(activity, CartActivity::class.java)
            activity.startActivity(intent)
            
            // Wait for cart to open, then proceed
            handler.postDelayed({
                onComplete()
            }, LONG_DELAY)
            
        }, SHORT_DELAY)
    }

    /**
     * Phase 4: Complete checkout
     */
    private fun simulateCheckoutPhase(activity: Activity, onComplete: () -> Unit) {
        Log.d(TAG, "Phase 4: Checkout...")
        
        // Navigate to checkout
        handler.postDelayed({
            val intent = Intent(activity, CheckoutActivity::class.java)
            activity.startActivity(intent)
            
            // Wait for checkout to open, then fill form
            handler.postDelayed({
                fillCheckoutForm(activity) {
                    // Submit order
                    handler.postDelayed({
                        submitOrder(activity, onComplete)
                    }, MEDIUM_DELAY)
                }
            }, LONG_DELAY)
            
        }, SHORT_DELAY)
    }

    /**
     * Fill the checkout form with simulated data
     */
    private fun fillCheckoutForm(activity: Activity, onComplete: () -> Unit) {
        Log.d(TAG, "Filling checkout form...")
        
        // Get current activity (should be CheckoutActivity)
        val currentActivity = getCurrentActivity(activity)
        
        handler.postDelayed({
            try {
                // Fill name
                currentActivity?.findViewById<EditText>(R.id.etName)?.let { field ->
                    simulateTyping(field, sampleNames.random())
                }
                
                handler.postDelayed({
                    // Fill email
                    currentActivity?.findViewById<EditText>(R.id.etEmail)?.let { field ->
                        simulateTyping(field, sampleEmails.random())
                    }
                    
                    handler.postDelayed({
                        // Fill address
                        currentActivity?.findViewById<EditText>(R.id.etAddress)?.let { field ->
                            simulateTyping(field, sampleAddresses.random())
                        }
                        
                        handler.postDelayed({
                            // Fill card number (fake)
                            currentActivity?.findViewById<EditText>(R.id.etCardNumber)?.let { field ->
                                simulateTyping(field, "4111111111111111")
                            }
                            
                            handler.postDelayed({
                                onComplete()
                            }, MEDIUM_DELAY)
                            
                        }, MEDIUM_DELAY)
                    }, MEDIUM_DELAY)
                }, MEDIUM_DELAY)
            } catch (e: Exception) {
                Log.e(TAG, "Error filling form: ${e.message}")
                onComplete()
            }
        }, SHORT_DELAY)
    }

    /**
     * Submit the order
     */
    private fun submitOrder(activity: Activity, onComplete: () -> Unit) {
        Log.d(TAG, "Submitting order...")
        
        val currentActivity = getCurrentActivity(activity)
        
        try {
            currentActivity?.findViewById<MaterialButton>(R.id.btnPlaceOrder)?.let { button ->
                button.performClick()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting order: ${e.message}")
        }
        
        // Wait for order to process and complete
        handler.postDelayed({
            // Navigate back to main screen
            handler.postDelayed({
                try {
                    val intent = Intent(activity, IceCreamMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating back: ${e.message}")
                }
                
                handler.postDelayed({
                    onComplete()
                }, MEDIUM_DELAY)
                
            }, LONG_DELAY)
        }, 3000L) // Wait for order API call to complete
    }

    /**
     * Simulate typing into an EditText
     */
    private fun simulateTyping(editText: EditText, text: String) {
        editText.requestFocus()
        editText.setText(text)
        editText.setSelection(text.length)
    }

    /**
     * Get the current foreground activity
     */
    private fun getCurrentActivity(fallback: Activity): Activity? {
        return try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val activitiesField = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            
            @Suppress("UNCHECKED_CAST")
            val activities = activitiesField.get(activityThread) as? Map<Any, Any>
            
            activities?.values?.forEach { activityRecord ->
                val activityRecordClass = activityRecord.javaClass
                val pausedField = activityRecordClass.getDeclaredField("paused")
                pausedField.isAccessible = true
                
                if (!pausedField.getBoolean(activityRecord)) {
                    val activityField = activityRecordClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    return activityField.get(activityRecord) as? Activity
                }
            }
            fallback
        } catch (e: Exception) {
            Log.w(TAG, "Could not get current activity: ${e.message}")
            fallback
        }
    }

    /**
     * Check if simulation is currently running
     */
    fun isSimulationRunning(): Boolean = isRunning.get()
}


package com.icecream.demo.api

import com.icecream.demo.model.CartItem
import com.icecream.demo.model.IceCream
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * API Service for Ice Cream Shop network operations.
 */
object IceCreamApiService {

    private val gson = Gson()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Base URLs for API endpoints
    private const val MENU_API_URL = "https://postman-echo.com/get?path=api/v1/menu"
    private const val ORDER_API_URL = "https://postman-echo.com/post"
    private const val INVENTORY_API_URL = "https://postman-echo.com/get?path=api/v1/inventory"

    /**
     * Fetch ice cream menu from the server.
     * Makes a real HTTP GET request.
     */
    fun fetchMenu(callback: (Result<List<IceCream>>) -> Unit) {
        val request = Request.Builder()
            .url(MENU_API_URL)
            .addHeader("Accept", "application/json")
            .addHeader("X-Api-Version", "1.0")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        // postman-echo returns our request info, we'll use local data
                        val flavors = IceCream.getSampleFlavors()
                        callback(Result.success(flavors))
                    } else {
                        callback(Result.failure(IOException("HTTP ${response.code}: ${response.message}")))
                    }
                }
            }
        })
    }

    /**
     * Check inventory availability for items.
     * Makes a real HTTP GET request.
     */
    fun checkInventory(iceCreamIds: List<Int>, callback: (Result<Map<Int, Int>>) -> Unit) {
        val idsParam = iceCreamIds.joinToString(",")
        val url = "$INVENTORY_API_URL?ids=$idsParam"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        // Return simulated inventory (all items in stock)
                        val inventory = iceCreamIds.associateWith { (10..50).random() }
                        callback(Result.success(inventory))
                    } else {
                        callback(Result.failure(IOException("HTTP ${response.code}: ${response.message}")))
                    }
                }
            }
        })
    }

    /**
     * Submit an order to the server.
     * Makes a real HTTP POST request with order data.
     */
    fun submitOrder(
        items: List<CartItem>,
        customerName: String,
        customerEmail: String,
        shippingAddress: String,
        callback: (Result<OrderResponse>) -> Unit
    ) {
        val orderRequest = OrderRequest(
            orderId = "ICE-${UUID.randomUUID().toString().take(8).uppercase()}",
            items = items.map { 
                OrderItem(
                    iceCreamId = it.iceCream.id,
                    name = it.iceCream.name,
                    quantity = it.quantity,
                    unitPrice = it.iceCream.price,
                    totalPrice = it.totalPrice
                )
            },
            customer = CustomerInfo(
                name = customerName,
                email = customerEmail,
                address = shippingAddress
            ),
            subtotal = items.sumOf { it.totalPrice },
            tax = items.sumOf { it.totalPrice } * 0.08,
            total = items.sumOf { it.totalPrice } * 1.08,
            timestamp = System.currentTimeMillis()
        )

        val jsonBody = gson.toJson(orderRequest)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(ORDER_API_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("X-Api-Version", "1.0")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        // Simulate successful order response
                        val orderResponse = OrderResponse(
                            success = true,
                            orderId = orderRequest.orderId,
                            message = "Order placed successfully!",
                            estimatedDelivery = "15-20 minutes"
                        )
                        callback(Result.success(orderResponse))
                    } else {
                        callback(Result.failure(IOException("HTTP ${response.code}: ${response.message}")))
                    }
                }
            }
        })
    }

    /**
     * Validate a promo code.
     * Makes a real HTTP GET request.
     */
    fun validatePromoCode(code: String, callback: (Result<PromoCodeResponse>) -> Unit) {
        val url = "https://postman-echo.com/get?path=api/v1/promo&code=$code"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        // Simulate promo code validation
                        val isValid = code.uppercase() in listOf("SWEET10", "ICECREAM20", "SUMMER15")
                        val discount = when (code.uppercase()) {
                            "SWEET10" -> 0.10
                            "ICECREAM20" -> 0.20
                            "SUMMER15" -> 0.15
                            else -> 0.0
                        }
                        callback(Result.success(PromoCodeResponse(isValid, discount, code)))
                    } else {
                        callback(Result.failure(IOException("HTTP ${response.code}: ${response.message}")))
                    }
                }
            }
        })
    }

    /**
     * Get order status.
     * Makes a real HTTP GET request.
     */
    fun getOrderStatus(orderId: String, callback: (Result<OrderStatusResponse>) -> Unit) {
        val url = "https://postman-echo.com/get?path=api/v1/orders/$orderId/status"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val statuses = listOf("Preparing", "Ready", "Out for Delivery")
                        callback(Result.success(OrderStatusResponse(
                            orderId = orderId,
                            status = statuses.random(),
                            updatedAt = System.currentTimeMillis()
                        )))
                    } else {
                        callback(Result.failure(IOException("HTTP ${response.code}: ${response.message}")))
                    }
                }
            }
        })
    }

    // Data classes for API requests/responses
    data class OrderRequest(
        val orderId: String,
        val items: List<OrderItem>,
        val customer: CustomerInfo,
        val subtotal: Double,
        val tax: Double,
        val total: Double,
        val timestamp: Long
    )

    data class OrderItem(
        val iceCreamId: Int,
        val name: String,
        val quantity: Int,
        val unitPrice: Double,
        val totalPrice: Double
    )

    data class CustomerInfo(
        val name: String,
        val email: String,
        val address: String
    )

    data class OrderResponse(
        val success: Boolean,
        val orderId: String,
        val message: String,
        val estimatedDelivery: String
    )

    data class PromoCodeResponse(
        val valid: Boolean,
        val discount: Double,
        val code: String
    )

    data class OrderStatusResponse(
        val orderId: String,
        val status: String,
        val updatedAt: Long
    )
}


package com.example.kiosk_m

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import com.google.firebase.auth.FirebaseAuth

class FirebaseManager {
    // Using the exact URL from your screenshot
    private val database = FirebaseDatabase.getInstance("https://my-kiosk-e95a6-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    private val auth = FirebaseAuth.getInstance()

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun saveCartItems(items: List<MenuItem>) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("cart").setValue(items)
    }

    fun getCartItemsOnce(onResult: (List<MenuItem>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("cart")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = mutableListOf<MenuItem>()
                for (child in snapshot.children) {
                    child.getValue(MenuItem::class.java)?.let { items.add(it) }
                }
                onResult(items)
            }
    }

    fun saveActiveOrders(orders: List<Order>) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("activeOrders").setValue(orders)
    }

    fun getActiveOrdersOnce(onResult: (List<Order>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("activeOrders")
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = mutableListOf<Order>()
                for (child in snapshot.children) {
                    child.getValue(Order::class.java)?.let { orders.add(it) }
                }
                onResult(orders)
            }
    }

    fun completeOrder(order: Order, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val orderId = database.child("users").child(userId).child("history").push().key ?: return
        val finalOrder = order.copy(id = orderId, timestamp = System.currentTimeMillis())

        // 1. Move to history
        database.child("users").child(userId).child("history").child(orderId).setValue(finalOrder)
            .addOnSuccessListener {
                // 2. Clear current cart after successful transaction
                database.child("users").child(userId).child("cart").removeValue()
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getOrderHistory(onResult: (List<Order>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("history")
            .get()
            .addOnSuccessListener { snapshot ->
                val history = mutableListOf<Order>()
                for (child in snapshot.children) {
                    child.getValue(Order::class.java)?.let { history.add(it) }
                }
                onResult(history.reversed()) // Newest first
            }
    }

    fun fetchUserData(onResult: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("firstName")
            .get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: "User"
                onResult(name)
            }
            .addOnFailureListener {
                onResult("User")
            }
    }

    fun getMenuItems(): Flow<List<MenuItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<MenuItem>()
                android.util.Log.d("FirebaseDebug", "Data received! Items found: ${snapshot.childrenCount}")
                for (child in snapshot.children) {
                    try {
                        val item = child.getValue(MenuItem::class.java)
                        if (item != null) {
                            items.add(item)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseDebug", "Error parsing item: ${child.key}", e)
                    }
                }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("FirebaseDebug", "Database Error: ${error.message}")
                close()
            }
        }

        database.child("menu_Jollibe").addValueEventListener(listener)
        awaitClose { database.child("menu_Jollibe").removeEventListener(listener) }
    }
}
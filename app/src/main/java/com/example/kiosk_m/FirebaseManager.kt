package com.example.kiosk_m

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseManager {
    fun fetchUserData(onResult: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val email = user.email
            if (email != null) {
                // Try to fetch the user's name from the Realtime Database
                val database = FirebaseDatabase.getInstance().getReference("user")
                database.orderByChild("email").equalTo(email).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                val name = userSnapshot.child("name").value?.toString()
                                if (!name.isNullOrEmpty()) {
                                    onResult(name)
                                    return@addOnSuccessListener
                                }
                            }
                        }
                        // Fallback to display name or email prefix if no name found in DB
                        onResult(user.displayName ?: email.substringBefore("@"))
                    }
                    .addOnFailureListener {
                        onResult(user.displayName ?: email.substringBefore("@"))
                    }
            } else {
                onResult(user.displayName ?: "User")
            }
        } else {
            onResult("Guest")
        }
    }
}

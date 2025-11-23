package com.example.dynamicwallpaper.Ui.auth.repository

import android.util.Log
import androidx.work.impl.constraints.trackers.NetworkStateTracker
import com.example.dynamicwallpaper.Common.NetworkResult
import com.example.dynamicwallpaper.Ui.auth.model.UserResponse
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    suspend fun login(email: String, password: String): NetworkResult<UserResponse> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            val firebaseUser = authResult.user ?: return NetworkResult.Error("User Not found", 0)
            NetworkResult.Success(
                UserResponse(
                    userName = firebaseUser.displayName ?: "",
                    userEmail = firebaseUser.email ?: "",
                    userImage = firebaseUser.photoUrl?.toString() ?: ""
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown Error", 0)
        }
    }

    suspend fun register(
        email: String, name: String, password: String
    ): NetworkResult<UserResponse> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser =
                authResult.user ?: return NetworkResult.Error("Registartion Failed", 0)
            saveUserInfo(UserResponse(name, email), firebaseUser)
            NetworkResult.Success(
                UserResponse(
                    userName = name, userEmail = email
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown Error", 0)
        }

    }

    suspend fun saveUserInfo(userData: UserResponse, firebaseUser: FirebaseUser) {
        val userData = hashMapOf(
            "name" to userData.userName,
            "email" to userData.userEmail,
            "photoUrl" to userData.userImage

        )

        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(firebaseUser.uid).set(userData).addOnSuccessListener {

            Log.d("RegisterFragment", "saveUserInfo: Success")
        }.addOnFailureListener { e ->
            // Handle errors
            Log.d("RegisterFragment", "saveUserInfo: $e")
        }

    }
}
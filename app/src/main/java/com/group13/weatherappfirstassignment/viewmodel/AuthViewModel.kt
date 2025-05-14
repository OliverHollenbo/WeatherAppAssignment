package com.group13.weatherappfirstassignment.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favoriteStationIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteStationIds: StateFlow<List<String>> = _favoriteStationIds

    fun signup(email: String, password: String, onSuccess: () -> Unit) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _loading.value = false
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        _error.value = task.exception?.localizedMessage
                    }
                }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _loading.value = false
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        _error.value = task.exception?.localizedMessage
                    }
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        _favoriteStationIds.value = emptyList()
        onSuccess()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun loadFavorites() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val favorites = snapshot.get("favorites") as? List<String>
                _favoriteStationIds.value = favorites ?: emptyList()
            }
            .addOnFailureListener {
                _favoriteStationIds.value = emptyList()
            }
    }

    fun addStationToFavorites(stationId: String) {
        val user = auth.currentUser ?: return
        val userDoc = db.collection("users").document(user.uid)
        userDoc.update("favorites", FieldValue.arrayUnion(stationId))
            .addOnSuccessListener { loadFavorites() }
            .addOnFailureListener {
                userDoc.set(mapOf("favorites" to listOf(stationId)))
                    .addOnSuccessListener { loadFavorites() }
            }
    }

    fun removeStationFromFavorites(stationId: String) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid)
            .update("favorites", FieldValue.arrayRemove(stationId))
            .addOnSuccessListener { loadFavorites() }
    }

    init {
        loadFavorites()
    }
}

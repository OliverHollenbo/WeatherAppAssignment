package com.group13.weatherappfirstassignment.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun addFavoriteStation(stationId: String) {
        userId?.let {
            db.collection("users")
                .document(it)
                .collection("favorites")
                .document(stationId)
                .set(mapOf("stationId" to stationId))
        }
    }

    fun removeFavoriteStation(stationId: String) {
        userId?.let {
            db.collection("users")
                .document(it)
                .collection("favorites")
                .document(stationId)
                .delete()
        }
    }

    fun getFavoriteStations(onComplete: (List<String>) -> Unit) {
        userId?.let {
            db.collection("users")
                .document(it)
                .collection("favorites")
                .get()
                .addOnSuccessListener { result ->
                    val favorites = result.documents.mapNotNull { it.getString("stationId") }
                    onComplete(favorites)
                }
        }
    }
}

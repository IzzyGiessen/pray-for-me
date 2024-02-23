package com.onlysaints.prayforme.database

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Database {
    private val db = Firebase.firestore
    private val ref = Firebase.database.getReferenceFromUrl("https://prayforme-7597f-default-rtdb.firebaseio.com/")

    fun addPrayer(prayer: Map<String, String>, onSuccess: (DocumentReference) -> Unit, onFailure: (Exception) -> Unit = {}) {
        db.collection("prayers")
            .add(prayer)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun getPrayers(onSuccess: (DataSnapshot) -> Unit, onFailure: (Exception) -> Unit = {}) {
        ref.child("prayers").get()
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun getPrayerById(id: String, onSuccess: (DocumentSnapshot) -> Unit, onFailure: (Exception) -> Unit = {}) {
        db.collection("prayers").document(id).get()
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun getPrayerCount(id: String, onSuccess: (DataSnapshot) -> Unit, onFailure: (Exception) -> Unit = {}) {
        ref.child("prayers").child(id).child("prayed_count").get()
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun incPrayerCount(id: String) {
        getPrayerCount(id, {
            println(it)
            val prayedCount = if(it.value == null) 0 else it.value as Long + 1
            ref.child("prayers").child(id).child("prayed_count").setValue(prayedCount)
        })
    }

    fun setPostTime(id: String) {
        ref.child("prayers").child(id).child("posted_time").setValue(System.currentTimeMillis())
    }
}
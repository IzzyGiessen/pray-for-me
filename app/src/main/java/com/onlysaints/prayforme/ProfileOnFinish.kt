package com.onlysaints.prayforme

import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot

class ProfileOnFinish(private val act: ProfileActivity) {
    private val prayers: MutableList<Map<String, String>> =  mutableListOf()

    fun getPrayerText(prayerDoc: DocumentSnapshot) {
        if (!prayerDoc.contains("prayer")) return
        val prayerText = prayerDoc["prayer"].toString()
        act.db.getPrayerCount(prayerDoc.id, addPrayerToAdapter(prayerDoc.id, prayerText))
    }

    fun removePrayer(holder: PrayerAdapter.ViewHolder) {
        val prayerId = holder.prayerId
        if (prayerId == null) return; // TODO: do something
        act.ls.remove(prayerId)
        holder.remove()
    }

    fun addPrayerToAdapter(prayerId: String, prayerText: String): (DataSnapshot) -> Unit = a@{count ->
        val prayer = HashMap<String, String>()
        prayer["prayer_id"] = prayerId
        prayer["prayer_text"] = prayerText
        prayer["prayed_count"] = count.value.toString().let { if(it == "null") "0" else it }
        act.ls.writePrayer(prayerId, prayer)
        prayers.add(prayer)
        //act.prayerRecycler.adapter = PrayerAdapter(act, prayers, this)
        act.prayerAdapter.addPrayer(prayer)
        // TODO: is this called every time?
        act.alternativeText.visibility = View.INVISIBLE
    }

    fun addPrayerToAdapter(prayerId: String, prayerText: String, prayedCount: String) {
        val prayer = HashMap<String, String>()
        println(prayerId)
        prayer["prayer_id"] = prayerId
        prayer["prayer_text"] = prayerText
        prayer["prayed_count"] = prayedCount
        act.ls.writePrayer(prayerId, prayer)
        // TODO: remove prayers variable
        prayers.add(prayer)
        //act.prayerRecycler.adapter = PrayerAdapter(act, prayers, this)
        act.prayerAdapter.addPrayer(prayer)
        act.alternativeText.visibility = View.INVISIBLE
    }

}
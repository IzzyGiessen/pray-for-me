package com.onlysaints.prayforme

import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

class ProfileOnFinish(private val act: ProfileActivity) {
    private val prayers: MutableList<Map<String, String>> =  mutableListOf()

    fun removePrayer(holder: PrayerAdapter.ViewHolder) {
        val prayerId = holder.prayerId
        if (prayerId == null) return; // TODO: do something
        act.ls.removePrayer(prayerId)
        holder.remove()
    }

    fun addCountToPrayer(prayer: MutableMap<String, String>): (DataSnapshot) -> Unit = {count ->
        prayer["prayer_count"] = count.value.toString().let { if(it == "null") "0" else it}
        act.addPrayerToAdapter(prayer)
    }


//    // TODO: why do we have two?
//    fun addPrayerToAdapter(prayerId: String, prayerText: String): (DataSnapshot) -> Unit = a@{count ->
//        println("PRAYER COUNT")
//        println(count.value)
//        val prayer = HashMap<String, String>()
//        prayer["prayer_id"] = prayerId
//        prayer["prayer_text"] = prayerText
//        prayer["prayer_count"] = count.value.toString().let { if(it == "null") "0" else it }
//        act.ls.writePrayer(prayer)
//        prayers.add(prayer)
//        //act.prayerRecycler.adapter = PrayerAdapter(act, prayers, this)
//        act.prayerAdapter.addPrayer(prayer)
//        // TODO: is this called every time?
//        act.alternativeText.visibility = View.INVISIBLE
//    }
//
//    fun addPrayerToAdapter(prayerId: String, prayerText: String, prayedCount: String) {
//        val prayer = HashMap<String, String>()
//        prayer["prayer_id"] = prayerId
//        prayer["prayer_text"] = prayerText
//        prayer["prayer_count"] = prayedCount
//        act.ls.writePrayer(prayer)
//        // TODO: remove prayers variable
//        prayers.add(prayer)
//        //act.prayerRecycler.adapter = PrayerAdapter(act, prayers, this)
//        act.prayerAdapter.addPrayer(prayer)
//        act.alternativeText.visibility = View.INVISIBLE
//    }

}
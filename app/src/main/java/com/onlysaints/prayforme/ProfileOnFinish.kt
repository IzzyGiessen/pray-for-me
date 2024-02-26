package com.onlysaints.prayforme

import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.onlysaints.prayforme.classes.Prayer

class ProfileOnFinish(private val act: ProfileActivity) {
    fun removePrayer(holder: PrayerAdapter.ViewHolder) {
        val prayerId = holder.prayerId
        if (prayerId == null) return; // TODO: do something
        act.ls.removePrayer(prayerId)
        holder.remove()
    }

    fun addCountToPrayer(prayer: Prayer): (DataSnapshot) -> Unit = { count ->
        val newPrayer = prayer.changeCount(count.value.toString().let { if(it == "null") 0 else it.toInt()})
        act.addPrayerToAdapter(newPrayer)
    }

}
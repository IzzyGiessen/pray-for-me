package com.onlysaints.prayforme.profile

import com.google.firebase.database.DataSnapshot
import com.onlysaints.prayforme.classes.Prayer

class ProfileOnFinish(private val frag: MyPrayersFragment) {
    fun removePrayer(holder: PrayerAdapter.ViewHolder) {
        val prayerId = holder.prayerId
        if (prayerId == null) return; // TODO: do something
        frag.ls.removePrayer(prayerId)
        holder.remove()
    }

    fun addCountToPrayer(prayer: Prayer): (DataSnapshot) -> Unit = { count ->
        val newPrayer = prayer.changeCount(count.value.toString().let { if(it == "null") 0 else it.toInt()})
        frag.addPrayerToAdapter(newPrayer)
    }

}
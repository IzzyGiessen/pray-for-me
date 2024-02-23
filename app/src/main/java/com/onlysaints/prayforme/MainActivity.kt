package com.onlysaints.prayforme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.onlysaints.prayforme.database.Database
import com.onlysaints.prayforme.listeners.ButtonTouchListener
import com.onlysaints.prayforme.listeners.PrayerStackListener


class MainActivity : AppCompatActivity() {
    val db = Database()

    // views
    lateinit var prayerCard1: ConstraintLayout
    lateinit var prayerCard2: ConstraintLayout
    lateinit var darkCover: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        darkCover = findViewById(R.id.dark_cover)
        prayerCard1 = findViewById(R.id.prayer_card_1)
        prayerCard2 = findViewById(R.id.prayer_card_2)
        val requestPrayersButton = findViewById<ImageView>(R.id.request_prayers_button)

        // set onTouchListeners
        requestPrayersButton.setOnTouchListener(ButtonTouchListener())
        prayerCard1.setOnTouchListener(PrayerStackListener(this))
    }

    fun requestPrayers(v: View) {
        intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

}

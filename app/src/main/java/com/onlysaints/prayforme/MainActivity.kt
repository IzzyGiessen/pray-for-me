package com.onlysaints.prayforme

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.onlysaints.prayforme.database.Database
import com.onlysaints.prayforme.listeners.ButtonTouchListener
import com.onlysaints.prayforme.listeners.PrayerStackListener


class MainActivity : AppCompatActivity() {
    val db = Database()
    lateinit var prayerStackListener: PrayerStackListener

    // views
    lateinit var mainLayout: ConstraintLayout
    lateinit var prayerCard1: ConstraintLayout
    lateinit var prayerCard2: ConstraintLayout
    lateinit var darkCover: View
    lateinit var prayerText: TextView
    lateinit var prayCount: TextView
    lateinit var readPopupView: View
    lateinit var leftSign: ConstraintLayout
    lateinit var rightSign: ConstraintLayout
    lateinit var openPrayerButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout = findViewById(R.id.main_layout)
        darkCover = findViewById(R.id.dark_cover)
        prayerCard1 = findViewById(R.id.prayer_card_1)
        prayerCard2 = findViewById(R.id.prayer_card_2)
        leftSign = findViewById(R.id.left_sign)
        rightSign = findViewById(R.id.right_sign)
        openPrayerButton = findViewById(R.id.open_prayer_button)

        val requestPrayersButton = findViewById<ImageView>(R.id.request_prayers_button)
        val openPrayerButton = findViewById<ImageButton>(R.id.open_prayer_button)

        // set onTouchListeners
        prayerStackListener = PrayerStackListener(this)
        requestPrayersButton.setOnTouchListener(ButtonTouchListener())
        openPrayerButton.setOnTouchListener(ButtonTouchListener())
        // only needs to be initialized for one card, because the listener switches
        prayerCard1.setOnTouchListener(prayerStackListener)

        // initialize read popup
        readPopupView = layoutInflater.inflate(R.layout.view_prayer_popup, mainLayout, false)
        prayerText = readPopupView.findViewById(R.id.prayer_text)
        prayCount = readPopupView.findViewById(R.id.pray_count)
        val readPopupMargin = readPopupView.findViewById<ConstraintLayout>(R.id.popup_margin)
        readPopupMargin.setOnClickListener{ removePopupView(readPopupView) }
    }

    fun openPrayer(v: View) {
        prayerText.text = prayerStackListener.prayerText
        prayCount.text = prayerStackListener.prayerCountText
        mainLayout.addView(readPopupView)
        val anim = AnimationUtils.loadAnimation(this, R.anim.popup_show)
        readPopupView.startAnimation(anim)
    }

    private fun removePopupView(v: View) {
        mainLayout.removeView(v)
        val anim = AnimationUtils.loadAnimation(this, R.anim.popup_show)
        v.startAnimation(anim)
    }

    fun requestPrayers(v: View) {
        intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

}

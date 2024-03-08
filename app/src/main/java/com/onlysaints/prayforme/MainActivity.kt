package com.onlysaints.prayforme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.onlysaints.prayforme.classes.Prayer
import com.onlysaints.prayforme.database.Database
import com.onlysaints.prayforme.listeners.PrayerStackListener
import com.onlysaints.prayforme.profile.ProfileActivity


class MainActivity : AppCompatActivity() {
    val db = Database()
    lateinit var prayerStackListener: PrayerStackListener

    // 1 to save prayer, 0 not to
    var savePrayer = 0
    lateinit var hearts: List<Int>

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
    lateinit var savePrayerButton: ImageView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hearts = listOf(
            R.drawable.empty_heart,
            R.drawable.heart
        )

        mainLayout = findViewById(R.id.main_layout)
        darkCover = findViewById(R.id.dark_cover)
        prayerCard1 = findViewById(R.id.prayer_card_1)
        prayerCard2 = findViewById(R.id.prayer_card_2)
        leftSign = findViewById(R.id.left_sign)
        rightSign = findViewById(R.id.right_sign)
        savePrayerButton = findViewById(R.id.save_prayer_button)

        // set onTouchListeners
        prayerStackListener = PrayerStackListener(this)
        // only needs to be initialized for one card, because the listener switches
        prayerCard1.setOnTouchListener(prayerStackListener)

        // initialize read popup
        readPopupView = layoutInflater.inflate(R.layout.view_prayer_popup, mainLayout, false)
        prayerText = readPopupView.findViewById(R.id.prayer_text)
        prayCount = readPopupView.findViewById(R.id.pray_count)
        val readPopupMargin = readPopupView.findViewById<ConstraintLayout>(R.id.popup_margin)
        readPopupMargin.setOnClickListener{ removePopupView(readPopupView) }
    }

    fun savePrayer(v: View) {
        savePrayer = 1 - savePrayer
        (v as ImageView).setBackgroundResource(hearts[savePrayer])
    }

    private fun removePopupView(v: View) {
        mainLayout.removeView(v)
        val anim = AnimationUtils.loadAnimation(this, R.anim.popup_show)
        v.startAnimation(anim)
    }

    fun requestPrayers(v: View) {
        // use Build.VERSION.RELEASE voor rooted and rommed devices according to StackOverflow
        val inAnim = androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom
        val outAnim = androidx.appcompat.R.anim.abc_fade_out

        if (Build.VERSION.SDK_INT > 34)
            overrideActivityTransition(androidx.appcompat.R.anim.abc_fade_in,
                inAnim, outAnim, 0)
        else
            overridePendingTransition(inAnim, outAnim)
        intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    companion object {
        var prayerList: ArrayList<Prayer> = ArrayList()
    }
}
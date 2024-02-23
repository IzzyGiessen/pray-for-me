package com.onlysaints.prayforme.listeners

import android.animation.TimeInterpolator
import android.content.res.Resources
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.onlysaints.prayforme.MainActivity
import com.onlysaints.prayforme.R

class PrayerStackListener(val act: MainActivity) : View.OnTouchListener {
    private var prayerId: String? = null
    private val cardMap = HashMap<ConstraintLayout, ConstraintLayout>()

    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private var cardWidth: Int = 0
    private val scaleFactor = 0.9f
    private val scaleDuration = 100L
    private val prayerTime = 7500L

    private var oX = 0f
    private var oY = 0f
    private var dX = 0f
    private var dY = 0f

    init {
        cardMap[act.prayerCard1] = act.prayerCard2
        cardMap[act.prayerCard2] = act.prayerCard1
        act.prayerCard1.post {
            cardWidth = act.prayerCard1.measuredWidth
        }
        loadPrayer(act.prayerCard1)
        loadPrayer(act.prayerCard2)
    }

    private fun loadPrayer(view: View) {
        act.db.getPrayers({allPrayers ->
            val prayerText = (view as ViewGroup).getChildAt(0) as TextView
            val prayers = allPrayers.children.toList()
            if (prayers.isEmpty()) {
                prayerText.text = "No prayers left... Request your own!"
                return@getPrayers
            }
            // pick prayer based on prayerCount and timestamp
            var prayer = prayers.random()
            for (i in 1..3) {
                val tempPrayer = prayers.random()
                prayer = prayer.comparePrayers(tempPrayer)
            }
            prayerId = prayer.key!!
            act.db.getPrayerById(prayerId!!, {
                prayerText.text = it["prayer"].toString()
            })
        })
    }

    private fun DataSnapshot.comparePrayers(other: DataSnapshot): DataSnapshot {
        val now = System.currentTimeMillis()
        fun DataSnapshot.prayedCount() = this.child("prayed_count").value as Long
        fun DataSnapshot.postedTime() = this.child("posted_time").value as Long
        fun DataSnapshot.score() = 1 / (now - postedTime()) * 1000 * 60 * prayedCount()

        return if (this.score() > other.score())
            this else other
    }

    private fun swipeRight(view: View) {
        val prayerText = (view as ViewGroup).getChildAt(0) as TextView
        prayerText.text = "..."
        loadPrayer(view)
        if (prayerId != null)
            act.db.incPrayerCount(prayerId!!)

        act.darkCover.isClickable = true
        act.darkCover.animate()
            .alpha(1f)
            .setDuration(prayerTime)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction{
                act.darkCover.animate()
                    .alpha(0f)
                    .setDuration(prayerTime)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction{
                        act.darkCover.isClickable = false
                    }.start()
            }.start()
    }

    private fun swipeLeft(view: View) {
        val prayerText = (view as ViewGroup).getChildAt(0) as TextView
        prayerText.text = "..."
        loadPrayer(view)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean = when(event.action) {
        MotionEvent.ACTION_DOWN -> onActionDown(view, event)
        MotionEvent.ACTION_MOVE -> onActionMove(view, event)
        MotionEvent.ACTION_UP -> onActionUp(view)
        else -> false
    }

    private fun onActionDown(view: View, event: MotionEvent): Boolean {
        oX = view.x
        oY = view.y
        dX = view.x - event.rawX
        dY = view.y - event.rawY
        view.animate()
            .scaleX(scaleFactor)
            .scaleY(scaleFactor)
            .setDuration(scaleDuration)
            .start()
        return true
    }

    private fun onActionMove(view: View, event: MotionEvent): Boolean {
        val animation: ViewPropertyAnimator = view.animate()
            .x(event.rawX + dX)
            .y(event.rawY + dY)
            .setDuration(0)
        animation.start()
        return true
    }

    private fun onActionUp(view: View): Boolean {
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(scaleDuration)
            .start()
        view.setOnTouchListener(null)
        if (view.x > screenWidth - cardWidth) {
            fadeCard(view, false)
        } else if(view.x < 0f) {
            fadeCard(view, true)
        } else {
            toOrigin(view, 100)
        }
        return true
    }

    private fun fadeCard(view: View, isLeft: Boolean) {
        val x = screenWidth.toFloat() * if (isLeft) -1 else 1
        view.animate()
            .x(x)
            .setDuration(100)
            .alpha(0f)
            .withEndAction{
                if (isLeft) swipeLeft(view) else swipeRight(view)
                switchCards(view)
            }
            .start()
    }

    private fun switchCards(view: View) {
        view.setOnTouchListener(null)
        toOrigin(view)
        view.z = 0f
        view.alpha = 1f
        val nextCard = cardMap[view]!!
        nextCard.z = 10f
        nextCard.setOnTouchListener(this)
    }

    private fun toOrigin(view: View, duration: Long = 0) {
        view.animate()
            .x(oX)
            .y(oY)
            .setDuration(duration)
            .withEndAction{ if(duration > 0) view.setOnTouchListener(this) }
            .start()
    }
}
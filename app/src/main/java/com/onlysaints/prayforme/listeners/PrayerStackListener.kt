package com.onlysaints.prayforme.listeners

import android.annotation.SuppressLint
import android.content.res.Resources
import android.opengl.Visibility
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.DataSnapshot
import com.onlysaints.prayforme.MainActivity
import com.onlysaints.prayforme.R
import org.w3c.dom.Text
import java.lang.Exception
import java.lang.Float.max
import java.lang.Float.min

class PrayerStackListener(val act: MainActivity) : View.OnTouchListener {
    private var prayerId: String? = null
    var prayerText: String? = null
    var prayerCountText: String? = null
    private val cardMap = HashMap<ConstraintLayout, ConstraintLayout>()

    // TODO: consider making static variables
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private var cardWidth: Int = 0
    private val scaleFactor = 0.9f
    private val scaleDuration = 100L
    private val prayerTime = 500L
    private val minAlpha = 0.3f
    private val backCardRotation = 5
    private val rotationDuration = 500L

    private var oX = 0f
    private var oY = 0f
    private var dX = 0f
    private var dY = 0f

    private var i = 0

    init {
        cardMap[act.prayerCard1] = act.prayerCard2
        cardMap[act.prayerCard2] = act.prayerCard1
        act.prayerCard1.post {
            cardWidth = act.prayerCard1.measuredWidth
        }
        loadPrayer(act.prayerCard1)
        loadPrayer(act.prayerCard2)

        act.prayerCard2.animate().rotation((-backCardRotation..backCardRotation).random().toFloat())
            .setDuration(rotationDuration).start()
    }

    private fun loadPrayer(view: View) {
        // TODO: clean up method
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE
        val prayerTextView = view.findViewById<TextView>(R.id.prayer_text)
        val prayerCountView = view.findViewById<TextView>(R.id.prayer_count)
        act.db.getPrayers({allPrayers ->
            val prayers = allPrayers.children.toList()
            if (prayers.isEmpty()) {
                // TODO: store string elsewhere
                prayerTextView.text = "No prayers left... Request your own!"
                return@getPrayers
            }
            // pick prayer based on prayerCount and timestamp
            var prayer = prayers.random()
            for (i in 1..3) {
                val tempPrayer = prayers.random()
                prayer = prayer.comparePrayers(tempPrayer)
            }
            // TODO: check if this is our own prayer
            prayerId = prayer.key
            act.db.getPrayerById(prayerId!!, {
                prayerText = cardMap[view]!!.findViewById<TextView>(R.id.prayer_text).text.toString()
                prayerCountText = cardMap[view]!!.findViewById<TextView>(R.id.prayer_count).text.toString()
                prayerTextView.text = it["text"].toString()
                progressBar.visibility = View.GONE
                prayerCountView.text = act.resources.getString(R.string.prayers_received,
                    prayer.child("prayer_count").value.toString())
            }, showPrayerLoadError(prayerTextView))
        }, showPrayerLoadError(prayerTextView))
    }

    private fun showPrayerLoadError(prayerText: TextView): (Exception) -> Unit = {
        it.printStackTrace()
        // TODO: store string elsewhere and make page reloadable
        prayerText.text = "Unable to load prayers. Please check your wifi connection and try again."
    }

    private fun DataSnapshot.comparePrayers(other: DataSnapshot): DataSnapshot {
        val now = System.currentTimeMillis()

        fun DataSnapshot.prayedCount() = this.child("prayer_count").value.let { if(it == null) 0 else it as Long }
        fun DataSnapshot.postedTime() = this.child("posted_time").value as Long
        fun DataSnapshot.score() = 1 / (now - postedTime()) * 1000 * 60 * prayedCount()

        return if (this.score() > other.score())
            this else other
    }

    private fun swipeRight(view: View) {
        swipeLeft(view)
        prayerId?.let { act.db.incPrayerCount(it) }


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
        // TODO: set a timeout for requests so the dots do not remain
        val prayerText = view.findViewById<TextView>(R.id.prayer_text)
        prayerText.text = ""
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
        val direction = if (event.rawX + dX - oX > 0) 1 else -1
        view.animate()
            .x(event.rawX + dX)
            .y(event.rawY + dY)
            .rotation((event.rawX + dX - oX)/15)
            .setDuration(0)
            .start()

        val alpha = max(minAlpha, direction * (event.rawX + dX - oX)/(screenWidth/4))
        val sign = if (direction == 1) act.rightSign else act.leftSign
        sign.animate().alpha(alpha).setDuration(0).start()
        return true
    }

    private fun onActionUp(view: View): Boolean {
        act.leftSign.animate().alpha(minAlpha).setDuration(100).start()
        act.rightSign.animate().alpha(minAlpha).setDuration(100).start()
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(0f)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun switchCards(view: View) {
        view.setOnTouchListener(null)
        toOrigin(view)
        val nextCard = cardMap[view]!!
        val tmp = nextCard.z
        nextCard.z = view.z
        view.z = tmp
        view.alpha = 1f
        view.animate().rotation((-backCardRotation..backCardRotation).random().toFloat())
            .setDuration(rotationDuration).start()
        nextCard.animate()
            .setDuration(100)
            .rotation(0f)
            .start()
        nextCard.setOnTouchListener(this)
        with (act.savePrayerButton) {
            (parent as ConstraintLayout).removeView(this)
            nextCard.addView(this)
            animate().scaleY(0f).scaleX(0f).setDuration(0).start()
            animate().scaleY(1f).scaleX(1f).setDuration(100).start()
            this.setBackgroundResource(R.drawable.empty_heart)
            val parent = this.parent as ConstraintLayout
            parent.removeView(this)
            nextCard.addView(this)
        }
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
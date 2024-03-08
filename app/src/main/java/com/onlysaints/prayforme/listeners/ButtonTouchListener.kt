package com.onlysaints.prayforme.listeners

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator

class ButtonTouchListener(private val clickType: ClickType) : View.OnTouchListener {
    private val scaleFactor = 1.2f
    private val duration = 50L
    private val longClickDuration = 400L
    private var holding = false

    override fun onTouch(view: View, event: MotionEvent): Boolean = when(event.action) {
        MotionEvent.ACTION_DOWN -> {
            if(clickType == ClickType.PRE_ANIM) {
                view.performClick()
            }
            view.animate()
                .scaleX(scaleFactor)
                .scaleY(scaleFactor)
                .setDuration(duration)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(duration)
                        .withEndAction {
                            if (clickType == ClickType.POST_ANIM) view.performClick() }
                }
            if(clickType == ClickType.HOLD) {
                holding = true
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (holding) {
                            holding = false
                            view.performLongClick()
                        }
                    }, longClickDuration
                )
            }
            true
        }
        MotionEvent.ACTION_UP -> {
            if (holding) view.performClick()
            holding = false
            true
        }
        else -> false
    }

    enum class ClickType(val id: Int) {
        HOLD(0), PRE_ANIM(1), POST_ANIM(2);

        companion object {
            fun fromInt(id: Int) = entries.first { it.id == id }

        }
    }
}
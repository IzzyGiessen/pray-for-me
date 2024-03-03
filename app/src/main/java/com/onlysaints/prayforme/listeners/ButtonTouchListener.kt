package com.onlysaints.prayforme.listeners

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

class ButtonTouchListener(val clickInstant: Boolean = false) : View.OnTouchListener {
    val scaleFactor = 1.2f
    val duration = 50L
    val longClickDuration = 400L
    var holding = false

    override fun onTouch(view: View, event: MotionEvent): Boolean = when(event.action) {
        MotionEvent.ACTION_DOWN -> {
            view.animate()
                .scaleX(scaleFactor)
                .scaleY(scaleFactor)
                .setDuration(duration)
                .withEndAction{
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(duration)
                        .start()
                }
                .start()
            if (clickInstant) {
                view.performClick()
            } else {
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

}
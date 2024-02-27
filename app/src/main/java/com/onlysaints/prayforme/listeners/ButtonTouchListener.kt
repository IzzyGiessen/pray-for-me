package com.onlysaints.prayforme.listeners

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

class ButtonTouchListener : View.OnTouchListener {
    val scaleFactor = 1.2f
    val duration = 50L
    val longClickDuration = 400L
    var holding = false

    override fun onTouch(view: View, event: MotionEvent): Boolean = when(event.action) {
        MotionEvent.ACTION_DOWN -> {
            holding = true
            view.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed(
                {if(holding) {
                    holding = false
                    view.performLongClick()
                }
            }, longClickDuration)
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
            true
        }
        MotionEvent.ACTION_UP -> {
            view.isClickable = true
            if (holding) view.performClick()
            holding = false
            true
        }
        else -> false
    }

}
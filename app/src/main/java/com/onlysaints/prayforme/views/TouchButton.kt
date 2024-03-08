package com.onlysaints.prayforme.views

import android.content.Context
import android.util.AttributeSet
import com.onlysaints.prayforme.R
import com.onlysaints.prayforme.listeners.ButtonTouchListener
import org.w3c.dom.Attr


class TouchImageButton : androidx.appcompat.widget.AppCompatImageButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initiateTouchListener(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initiateTouchListener(attrs)
    }

    private fun initiateTouchListener(attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.TouchImageButton, 0, 0)
        if(a.hasValue(R.styleable.TouchImageButton_click_type)) println("HAS")
        val clickType = a.getInt(R.styleable.TouchImageButton_click_type, 0)
        println(clickType)
        setOnTouchListener(ButtonTouchListener(ButtonTouchListener.ClickType.fromInt(clickType)))
        a.recycle()
    }
}
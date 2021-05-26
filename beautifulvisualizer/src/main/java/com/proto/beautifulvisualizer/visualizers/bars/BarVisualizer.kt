package com.proto.beautifulvisualizer.visualizers.bars

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.proto.beautifulvisualizer.BaseVisualizer
import com.proto.beautifulvisualizer.R

class BarVisualizer @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseVisualizer(context, attrs, defStyleAttr) {

    init {
        // Setting view attrs
        if (attrs != null) {
            val typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.BarVisualizer, 0, 0)

            typedArray.recycle()
        }
    }

    override fun updateFrame(values: IntArray, canvas: Canvas) {

    }
}
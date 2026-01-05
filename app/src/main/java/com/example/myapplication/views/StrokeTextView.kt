package com.example.myapplication.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import com.example.myapplication.R

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeColor: Int = 0
    private var strokeWidth: Float = 0f

    init {
        context.withStyledAttributes(attrs, R.styleable.StrokeTextView) {
            strokeColor = getColor(R.styleable.StrokeTextView_strokeColor, 0)
            strokeWidth = getDimension(R.styleable.StrokeTextView_strokeWidth, 0f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val textColor = textColors

        val paint = paint
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 10f
        paint.strokeWidth = strokeWidth

        setTextColor(strokeColor)
        super.onDraw(canvas)

        paint.style = Paint.Style.FILL

        setTextColor(textColor)
        super.onDraw(canvas)
    }
}
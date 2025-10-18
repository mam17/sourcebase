package com.example.myapplication.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.ScaleAnimation
import androidx.appcompat.widget.AppCompatImageView
import com.example.myapplication.R
import kotlin.let

class RoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var enableScaleAnim: Boolean = true
    private var cornerRadius: Float = 0f
    private var isOval: Boolean = false
    private var bgColor: Int = Color.TRANSPARENT
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rectF = RectF()

    init {
        isClickable = true
        scaleType = ScaleType.CENTER_CROP
        setWillNotDraw(false)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.RoundImageView)
            cornerRadius = ta.getDimension(R.styleable.RoundImageView_cornerRadius, 0f)
            isOval = ta.getBoolean(R.styleable.RoundImageView_isOval, false)
            bgColor = ta.getColor(R.styleable.RoundImageView_bgColor, Color.TRANSPARENT)
            enableScaleAnim = ta.getBoolean(R.styleable.RoundImageView_enableScaleAnim, true)

            if (ta.hasValue(R.styleable.RoundImageView_iconTint)) {
                val tintColor = ta.getColor(R.styleable.RoundImageView_iconTint, Color.TRANSPARENT)
                setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            }

            ta.recycle()
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        setImageBitmap(bitmap)
        invalidate()
    }

    fun setCornerRadius(radius: Float) {
        this.cornerRadius = radius
        invalidate()
    }

    fun setOval(enable: Boolean) {
        this.isOval = enable
        invalidate()
    }

    fun setBgColor(color: Int) {
        this.bgColor = color
        invalidate()
    }

    fun setEnableScaleAnim(enable: Boolean) {
        this.enableScaleAnim = enable
    }

    fun setIconTint(color: Int) {
        setColorFilter(color, PorterDuff.Mode.SRC_IN)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        path.reset()

        if (isOval) {
            path.addOval(rectF, Path.Direction.CW)
        } else if (cornerRadius > 0f) {
            path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        } else {
            path.addRect(rectF, Path.Direction.CW)
        }

        canvas.clipPath(path)

        if (bgColor != Color.TRANSPARENT) {
            paint.color = bgColor
            canvas.drawRect(rectF, paint)
        }

        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enableScaleAnim) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startScaleAnim(0.95f)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> startScaleAnim(1f)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startScaleAnim(scale: Float) {
        val anim = ScaleAnimation(
            scaleX, scale, scaleY, scale,
            (width / 2).toFloat(), (height / 2).toFloat()
        )
        anim.duration = 120
        anim.fillAfter = true
        startAnimation(anim)
    }
}
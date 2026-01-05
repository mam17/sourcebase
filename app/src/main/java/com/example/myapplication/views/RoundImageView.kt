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
    private var enableStroke: Boolean = false
    private var strokeWidth: Float = 0f
    private var strokeColor: Int = Color.TRANSPARENT
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    init {
        isClickable = true
        setWillNotDraw(false)

        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.RoundImageView)
            cornerRadius = ta.getDimension(R.styleable.RoundImageView_cornerRadius, 0f)
            isOval = ta.getBoolean(R.styleable.RoundImageView_isOval, false)
            bgColor = ta.getColor(R.styleable.RoundImageView_bgColor, Color.TRANSPARENT)
            enableScaleAnim = ta.getBoolean(R.styleable.RoundImageView_enableScaleAnim, true)
            enableStroke = ta.getBoolean(R.styleable.RoundImageView_enableStroke, false)
            strokeWidth = ta.getDimension(R.styleable.RoundImageView_imageStrokeWidth, 0f)
            strokeColor = ta.getColor(R.styleable.RoundImageView_imageStrokeColor, Color.TRANSPARENT)

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

    fun setStrokeEnable(enable: Boolean) {
        this.enableStroke = enable
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        this.strokeWidth = width
        invalidate()
    }

    fun setStrokeColor(color: Int) {
        this.strokeColor = color
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

        val save = canvas.save()
        canvas.clipPath(path)

        if (bgColor != Color.TRANSPARENT) {
            paint.color = bgColor
            canvas.drawRect(rectF, paint)
        }

        super.onDraw(canvas)

        canvas.restoreToCount(save)

        if (enableStroke && strokeWidth > 0f && strokeColor != Color.TRANSPARENT) {
            strokePaint.color = strokeColor
            strokePaint.strokeWidth = strokeWidth

            val half = strokeWidth / 2f
            val strokeRect = RectF(
                half,
                half,
                width - half,
                height - half
            )

            val radius = (cornerRadius - half).coerceAtLeast(0f)

            if (isOval) {
                canvas.drawOval(strokeRect, strokePaint)
            } else if (cornerRadius > 0f) {
                canvas.drawRoundRect(strokeRect, radius, radius, strokePaint)
            } else {
                canvas.drawRect(strokeRect, strokePaint)
            }
        }
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
            1f, scale,
            1f, scale,
            (width / 2).toFloat(),
            (height / 2).toFloat()
        )
        anim.duration = 120
        anim.fillAfter = true
        startAnimation(anim)
    }

}
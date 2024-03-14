package com.wantique.lockscreen.dnd

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.wantique.lockscreen.R
import java.lang.IllegalStateException
import kotlin.math.pow


class DragAndDrop @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var dndDesiredWidth: Int = 0
    private var dndDesiredHeight: Int = 0
    private var circleRadius: Float = 0f
    private var areaRadius: Float = 0f
    private var isValidTouch: Boolean = false
    private var dragAxisX: Float = 0f
    private var dragAxisY: Float = 0f
    private var deltaX: Float = 0f
    private var deltaY: Float = 0f
    private var handleDrawable: Drawable? = null

    private val dragBoundsRect = RectF()
    private val leftAreaBoundsRect = RectF()
    private val rightAreaBoundsRect = RectF()

    private val circlePaint = Paint()
    private val areaPaint = Paint()
    private val handlePaint = Paint()
    private lateinit var onRightDragListener: () -> Unit
    private lateinit var onLeftDragListener: () -> Unit

    //private val handleBitmap: Bitmap

    init {
        dndDesiredWidth = (getDeviceDensity(context) * 280).toInt()
        dndDesiredHeight = (getDeviceDensity(context) * 150).toInt()
        circleRadius = (getDeviceDensity(context) * 36)
        areaRadius = (getDeviceDensity(context) * 120)
        handleDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_default_handle, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        viewWidth = when(widthMode) {
            MeasureSpec.EXACTLY -> {
                if(context.resources.displayMetrics.widthPixels == widthSize) {
                    widthSize
                } else {
                    throw IllegalStateException("the width of DragAndDrop view must be the same as screen width.")
                }
            }
            else -> throw IllegalStateException("layout_width is allowed only match_parent or match_constraint")
        }

        viewHeight = when(heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(dndDesiredHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> dndDesiredHeight
            else -> dndDesiredHeight
        }

        initBounds()
        setMeasuredDimension(viewWidth, viewHeight)
    }

    private fun initBounds() {
        dragAxisX = (viewWidth / 2).toFloat()
        dragAxisY = viewHeight - areaRadius

        dragBoundsRect.left = dragAxisX - circleRadius
        dragBoundsRect.right = dragAxisX + circleRadius
        dragBoundsRect.top = dragAxisY - circleRadius
        dragBoundsRect.bottom = dragAxisY + circleRadius

        rightAreaBoundsRect.left = viewWidth.toFloat() - areaRadius
        rightAreaBoundsRect.right = viewWidth.toFloat() + areaRadius
        rightAreaBoundsRect.top = viewHeight.toFloat() - areaRadius
        rightAreaBoundsRect.bottom = viewHeight.toFloat() + areaRadius

        leftAreaBoundsRect.left = -areaRadius
        leftAreaBoundsRect.right = areaRadius
        leftAreaBoundsRect.top = viewHeight.toFloat() - areaRadius
        leftAreaBoundsRect.bottom = viewHeight.toFloat() + areaRadius
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        areaPaint.color = context.resources.getColor(R.color.blue, null)
        canvas.drawCircle(0f, viewHeight.toFloat(), areaRadius, areaPaint)
        canvas.drawCircle(viewWidth.toFloat(), viewHeight.toFloat(), areaRadius, areaPaint)

        handleDrawable?.let {
            it.setBounds(
                ((dragAxisX - (it.intrinsicWidth / 2)) + deltaX).toInt(),
                ((dragAxisY - (it.intrinsicHeight / 2)) + deltaY).toInt(),
                ((dragAxisX + (it.intrinsicWidth / 2)) + deltaX).toInt(),
                ((dragAxisY + (it.intrinsicHeight / 2)) + deltaY).toInt()
            )
            it.draw(canvas)
        }
    }

    private fun getDeviceDensity(context: Context): Float {
        val displayMetrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.density
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handleDownEvent(event)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if(isValidTouch) {
                    handleMoveEvent(event)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                handleUpEvent(event)
                return true
            }
        }

        return false
    }

    private fun handleDownEvent(event: MotionEvent) {
        isValidTouch = if(event.x >= dragBoundsRect.left && event.x <= dragBoundsRect.right && event.y <= dragBoundsRect.bottom && event.y >= dragBoundsRect.top) {
            true
        } else {
            false
        }
    }

    private fun handleMoveEvent(event: MotionEvent) {
        deltaX = event.x - dragAxisX
        deltaY = event.y - dragAxisY

        invalidate()
    }

    private fun handleUpEvent(event: MotionEvent) {
        if(isTouchedInsideArea(event.x, event.y, 0f, viewHeight.toFloat())) {
            onLeftDragListener()
            //Toast.makeText(context, "inside left", Toast.LENGTH_SHORT).show()
        }

        if(isTouchedInsideArea(event.x, event.y, viewWidth.toFloat(), viewHeight.toFloat())) {
            onRightDragListener()
            //Toast.makeText(context, "inside right", Toast.LENGTH_SHORT).show()
        }

        deltaX = 0f
        deltaY = 0f
        invalidate()
    }

    private fun isTouchedInsideArea(x: Float, y: Float, areaX: Float, areaY: Float): Boolean {
        val distance = Math.sqrt((x - areaX).toDouble().pow(2) + (y - areaY).toDouble().pow(2))
        return distance <= areaRadius
    }

    fun setOnDragListener(onRightDragListener: () -> Unit, onLeftDragListener: () -> Unit) {
        this.onRightDragListener = onRightDragListener
        this.onLeftDragListener = onLeftDragListener
    }
}
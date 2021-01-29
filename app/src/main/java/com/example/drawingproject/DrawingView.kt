package com.example.drawingproject

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    //drawing path
    private var drawPath: CustomPath? = null

    //drawing and canvas paint
    var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null

    //initial color
    private var paintColor = Color.BLACK

    //set alpha
    //return current alpha
    private var paintAlpha = 255


    //canvas
    var drawCanvas: Canvas? = null

    //canvas bitmap
    private var canvasBitmap: Bitmap? = null

    //brush sizes
    private var brushSize = 0f

    //get and set last brush size
    var lastBrushSize = 0f

    //erase flag
    private var erase = false

    private val paths = ArrayList<CustomPath>()
    private val undonePaths = ArrayList<CustomPath>()
    //setup drawing
    private fun setupDrawing() {
        brushSize = 20.toFloat()
        lastBrushSize = brushSize
        drawPath = CustomPath(paintColor, brushSize,paintAlpha)
        drawPaint = Paint()
        drawPaint!!.color = paintColor
        drawPaint!!.isAntiAlias = true
        drawPaint!!.strokeWidth = brushSize
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    //size assigned to view
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
    }

    //draw the view - will be called after touch event
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        for (path in paths) {
            drawPaint?.strokeWidth = path.brushThickness
            drawPaint?.color = path.color
            drawPaint?.alpha = path.alpha
            canvas.drawPath(path,drawPaint!!)
        }
        if (!drawPath!!.isEmpty && !erase){
            drawPaint?.strokeWidth = drawPath!!.brushThickness
            drawPaint?.color = drawPath!!.color
            drawPaint?.alpha = drawPath!!.alpha
            canvas.drawPath(drawPath!!,drawPaint!!)
        }
    }

    //register user touches as drawing action
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath?.brushThickness= brushSize
                drawPath?.color = paintColor
                drawPath?.alpha = paintAlpha

                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.moveTo(touchX, touchY)
                    }
                }


            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        drawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                paths.add(drawPath!!)

                drawPath = CustomPath(paintColor, brushSize,paintAlpha)

            }
            else -> return false
        }
        //redraw
        invalidate()
        return true
    }

    //update color
    fun setColor(newColor: Int) {
        paintColor = newColor
        drawPaint!!.color = paintColor
    }

    //set brush size
    fun setSizeForBrush(newSize: Float) {
       brushSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, newSize,
                resources.displayMetrics
        )
        drawPaint!!.strokeWidth =brushSize
    }

    //set erase true or false
//    fun setErase(isErase: Boolean) {
//        erase = isErase
//        if (erase) drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) else drawPaint!!.xfermode = null
//    }
    fun setPaintAlpha(newAlpha: Int) {
        paintAlpha = Math.round(newAlpha.toFloat() / 100 * 255)
        drawPaint!!.color = paintColor
        drawPaint!!.alpha = paintAlpha
    }
    fun onClickUndo() {
        if (paths.size > 0) {
            undonePaths.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }
    init {
        setupDrawing()
    }
    internal inner class CustomPath(var color: Int, var brushThickness: Float,var alpha:Int) : Path()
}








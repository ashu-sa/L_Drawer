package com.example.ldrawer

import android.app.usage.UsageEvents
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ContentInfoCompat

class DrawingActivity(context:Context,attrs:AttributeSet): View(context,attrs) {

    private var mDrawPath:CustomPath? = null
    private var mCanvasPaint:Paint?= null
    private var mCanvasBitmap:Bitmap?= null
    private var mDrawPaint:Paint?= null
    private var mBrushSize: Float = 0.toFloat()
    private var color =Color.BLACK
    private var canvas:Canvas? =null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()
    init {
        setUpDrawing()
    }


    private fun setUpDrawing(){
        mDrawPaint=Paint()
        mDrawPath= CustomPath(color,mBrushSize)
        mDrawPaint!!.color=color
        mDrawPaint!!.style= Paint.Style.STROKE
        mDrawPaint!!.strokeJoin= Paint.Join.ROUND
        mDrawPaint!!.strokeCap= Paint.Cap.ROUND
        mCanvasPaint= Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
   }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        for (path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushSize   //this is because we can draw with new colour and new BrushSize
            mDrawPaint!!.color = path.Colour
            canvas?.drawPath(path,mDrawPaint!!)
        }

        //If we didn't start drawing anything

        if (!mDrawPath!!.isEmpty){
        mDrawPaint!!.strokeWidth = mDrawPath!!.brushSize
        mDrawPaint!!.color = mDrawPath!!.Colour
        canvas?.drawPath(mDrawPath!!,mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        var touchX = event?.x
        var touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.Colour = color
                mDrawPath!!.brushSize = mBrushSize

                mDrawPath!!.reset()

                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false

        }

        invalidate()
        return true
    }


    fun setSizeBrush(newSize:Float){
        mBrushSize= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
         //we can simply set mBrushSize= newSize but for diffrent device we have to made this code
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setPaintColors(newColor:String){
        color= Color.parseColor(newColor)
        mDrawPaint!!.color= color
    }
    internal inner class CustomPath(var Colour:Int, var brushSize: Float):Path() {

    }
    fun onUndoClick(){
//        this method is used to check is there any path available or not
        if (mPaths.size>0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
        }
        invalidate()
        //which will internally call onDraw() method
    }


}
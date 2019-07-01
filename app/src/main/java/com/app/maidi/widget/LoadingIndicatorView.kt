package com.app.maidi.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.app.maidi.R

class LoadingIndicatorView : View {

    //indicators indicator
    val BallSpinFadeLoader = 22

    annotation class Indicator{

    }

    //Sizes (with defaults in DP)
    val DEFAULT_SIZE = 45

    //attrs
     var mIndicatorId: Int = 0
     var mIndicatorColor: Int = 0

    lateinit var mPaint: Paint

    lateinit var mIndicatorController: BaseIndicatorController

    private var mHasAnimation: Boolean = false

    constructor(context: Context) : super(context){
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr : Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr : Int, defStyleRes: Int ) : super(context, attrs, defStyleAttr, defStyleRes){
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.AVLoadingIndicatorView)
        mIndicatorId = a.getInt(
            R.styleable.AVLoadingIndicatorView_indicator, BallSpinFadeLoader
        )//Get number properties
        mIndicatorColor = a.getColor(
            R.styleable.AVLoadingIndicatorView_indicator_color, Color.WHITE
        )//Get color properties
        a.recycle()//Collection of recycling attributes
        mPaint = Paint()
        mPaint.color = mIndicatorColor//Set the color of the brush
        mPaint.style = Paint.Style.FILL//Set the style for the brush to fill
        mPaint.isAntiAlias = true//De aliasing
        applyIndicator()//
    }

    private fun applyIndicator() {
        when (mIndicatorId) {
            BallSpinFadeLoader -> mIndicatorController = BallSpinFadeLoaderIndicator()
        }
        mIndicatorController.setTarget(this)//Set the control to the currentView
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(dp2px(DEFAULT_SIZE), widthMeasureSpec)//ObtainViewWidth
        val height = measureDimension(dp2px(DEFAULT_SIZE), heightMeasureSpec)//ObtainViewHeight
        setMeasuredDimension(width, height)//
    }

    private fun measureDimension(defaultSize: Int, measureSpec: Int): Int {
        var result = defaultSize
        val specMode = View.MeasureSpec.getMode(measureSpec)//Measurement specification
        val specSize = View.MeasureSpec.getSize(measureSpec)//Measure size
        if (specMode == View.MeasureSpec.EXACTLY) {  //Parent control has set determine the size of the child control,
            // Child control will consider the size of the parent control,
            //How much you need to set up
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) { //Child controls can set their own desired size
            result = Math.min(defaultSize, specSize)//Minimum value
        } else {
            result = defaultSize
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawIndicator(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!mHasAnimation) {
            mHasAnimation = true
            applyAnimation()
        }
    }

    internal fun drawIndicator(canvas: Canvas) {
        mIndicatorController.draw(canvas, mPaint)
    }

    internal fun applyAnimation() {
        mIndicatorController.createAnimation()
    }

    private fun dp2px(dpValue: Int): Int {
        return context.resources.displayMetrics.density.toInt() * dpValue
    }
}
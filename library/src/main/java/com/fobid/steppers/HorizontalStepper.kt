package com.fobid.steppers

import android.content.Context
import android.support.annotation.IntRange
import android.support.constraint.ConstraintLayout
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Created by sungtae.lim on 2018. 3. 8..
 */

class HorizontalStepper : ConstraintLayout, ViewPager.OnPageChangeListener {

    private var orderViews: Array<View?>? = null
    private var progressBars: Array<ProgressBar?>? = null
    private var titleTextViews: Array<TextView?>? = null
    private var pager: ViewPager? = null

    var onStepClickListener: OnStepClickListener? = null

    private var prevPosition: Int = 0

    private val isValidate: Boolean
        get() {
            if (pager == null) {
                return false
            }
            val adapter = pager!!.adapter ?: return false

            val count = adapter.count

            return orderViews!!.size == count && progressBars!!.size == count - 1 && titleTextViews!!.size == count
        }

    interface OnStepClickListener {
        fun onStepClick(position: Int)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setPager(pager: ViewPager?) {
        this.pager = pager
        setOnPageChangeListener()
        drawStepper()
    }

    private fun setOnPageChangeListener() {
        if (pager != null) {
            pager!!.addOnPageChangeListener(this)
        }
    }

    private fun drawStepper() {
        removeAllViews()

        if (pager == null || pager!!.adapter == null) {
            return
        }

        val adapter = pager!!.adapter
        val count = adapter.count

        orderViews = arrayOfNulls(count)
        progressBars = arrayOfNulls(count - 1)
        titleTextViews = arrayOfNulls(count)

        if (count == 0) {
            return
        }

        val pageTitles = arrayOfNulls<CharSequence>(count)

        for (i in 0 until count) {

            val orderView = LayoutInflater.from(context).inflate(R.layout.content_stepper_order, null)
            orderView.id = ORDER_VIEW_ID + i

            orderViews!![i] = orderView

            val orderText = orderView.findViewById<TextView>(R.id.stepper_order_text)

            orderText.text = (i + 1).toString()

            if (i > 0) {
                val progressView = LayoutInflater.from(context).inflate(R.layout.content_stepper_progress, null)
                progressView.id = PROGRESS_VIEW_ID + i - 1

                progressBars!![i - 1] = progressView.findViewById(R.id.stepper_progress)

                val progressBarLayoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                progressBarLayoutParams.startToEnd = orderViews!![i - 1]!!.id
                progressView.layoutParams = progressBarLayoutParams

                addView(progressView)

                val orderTextViewLayoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                orderTextViewLayoutParams.startToEnd = progressView.id

                orderViews!![i]!!.layoutParams = orderTextViewLayoutParams
            }

            orderViews!![i]!!.setOnClickListener {
                pager!!.setCurrentItem(i, true)

                if (onStepClickListener != null) {
                    onStepClickListener!!.onStepClick(i)
                }
            }

            addView(orderViews!![i])

            pageTitles[i] = adapter.getPageTitle(i)

            if (pageTitles[i] != null) {
                val titleTextViewId = TITLE_TEXT_VIEW_ID + i

                titleTextViews!![i] = TextView(context)
                titleTextViews!![i]!!.id = titleTextViewId
                titleTextViews!![i]!!.text = pageTitles[i]

                val titleTextViewLayoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                titleTextViewLayoutParams.topToBottom = orderView.id

                titleTextViews!![i]!!.layoutParams = titleTextViewLayoutParams

                addView(titleTextViews!![i])

                if (i > 0) {
                    titleTextViewLayoutParams.startToEnd = titleTextViews!![i - 1]!!.id
                    titleTextViews!![i]!!.layoutParams = titleTextViewLayoutParams
                }
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (progressBars!!.isNotEmpty()) {
            when {
                positionOffset == 0f -> {
                    //                progressBars[position].setProgress((int) (positionOffset * 100));
                }
                prevPosition < position -> // swipe to left
                    progressBars!![prevPosition]!!.progress = (positionOffset * 100).toInt()
                else -> // swipe to right
                    progressBars!![position]!!.progress = (positionOffset * 100).toInt()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        prevPosition = position

        if (!isValidate) {
            return
        }
        val count = pager!!.adapter.count

        for (i in 0 until count) {
            orderViews!![i]!!.isSelected = i == position
        }
    }


    override fun onPageScrollStateChanged(state: Int) {}

    fun setStep(@IntRange(from = 0) position: Int) {
        prevPosition = position

        if (position < 0 || !isValidate) {
            return
        }

        if (position >= pager!!.adapter.count) {
            throw IndexOutOfBoundsException()
        }

        val count = pager!!.adapter.count

        for (i in 0 until count) {
            orderViews!![i]!!.isSelected = i == position
        }

        pager!!.currentItem = position
    }

    companion object {

        private const val ORDER_VIEW_ID = 1000
        private const val TITLE_TEXT_VIEW_ID = 2000
        private const val PROGRESS_VIEW_ID = 3000
    }
}

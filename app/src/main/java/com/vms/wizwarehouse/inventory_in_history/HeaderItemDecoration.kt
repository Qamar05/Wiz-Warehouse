package com.vms.wizwarehouse.inventory_in_history

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R

class HeaderItemDecoration(
        context: Context,
        private val mListener: StickyHeaderInterface
) : RecyclerView.ItemDecoration() {

    private val headerView: View
    private val headerTextView: TextView
    private val headerHeight: Int
    private val paint: Paint

    init {
        val inflater = LayoutInflater.from(context)
        headerView = inflater.inflate(R.layout.item_sticky_header, FrameLayout(context), false)
        headerTextView = headerView.findViewById(R.id.headerText)

        // Measure header once
        headerView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        headerHeight = headerView.measuredHeight

        paint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.GRAY
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount == 0) return

                val leftMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f, parent.context.resources.displayMetrics
        ).toInt()

        var previousHeader = ""
        var currentHeader: String?

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue

            currentHeader = mListener.getHeaderForPosition(position)
            if (previousHeader != currentHeader) {
                drawHeader(canvas, child, currentHeader, parent, leftMargin)
                previousHeader = currentHeader
            }
        }

        // Sticky logic
        val topChild = parent.getChildAt(0)
        val topPosition = parent.getChildAdapterPosition(topChild)
        if (topPosition == RecyclerView.NO_POSITION) return

                val topHeader = mListener.getHeaderForPosition(topPosition)
        val stickyHeaderView = mListener.getHeaderView(topHeader, parent) ?: return

                stickyHeaderView.measure(
                        View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
        stickyHeaderView.layout(0, 0, parent.width, stickyHeaderView.measuredHeight)

        val contactPoint = headerHeight
        val nextChild = getNextHeaderView(parent, topPosition)
        var offset = 0

        nextChild?.let {
            val nextChildTop = it.top
            if (nextChildTop < contactPoint) {
                offset = nextChildTop - contactPoint
            }
        }

        canvas.save()
        canvas.translate(leftMargin.toFloat(), offset.toFloat())
        stickyHeaderView.draw(canvas)
        canvas.restore()
    }

    private fun drawHeader(canvas: Canvas, child: View, headerText: String, parent: RecyclerView, top: Int) {
        val headerView = mListener.getHeaderView(headerText, parent) ?: return

                headerView.measure(
                        View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
        headerView.layout(0, 0, parent.width, headerView.measuredHeight)

        val startMarginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f, parent.context.resources.displayMetrics
        ).toInt()

        canvas.save()
        canvas.translate(startMarginPx.toFloat(), (child.top - headerView.height).toFloat())
        headerView.draw(canvas)
        canvas.restore()
    }

    interface StickyHeaderInterface {
        fun getHeaderForPosition(position: Int): String
        fun isHeader(position: Int): Boolean
        fun getHeaderView(headerText: String, parent: RecyclerView): View?
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

                val currentHeader = mListener.getHeaderForPosition(position)
        val nextHeader = if (position + 1 < state.itemCount) {
            mListener.getHeaderForPosition(position + 1)
        } else {
            ""
        }

        if (currentHeader != nextHeader) {
            val spaceInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 30f, parent.context.resources.displayMetrics
            ).toInt()
            outRect.bottom = spaceInPx
        } else {
            outRect.bottom = 0
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val itemCount = state.itemCount

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue

            val currentHeader = mListener.getHeaderForPosition(position)
            val nextHeader = if (position + 1 < itemCount) {
                mListener.getHeaderForPosition(position + 1)
            } else {
                ""
            }

            if (currentHeader != nextHeader) {
                val left = child.left + child.paddingLeft.toFloat()
                val right = child.right - child.paddingRight.toFloat()
                val y = child.bottom + child.paddingBottom + 10f

                canvas.drawLine(left, y, right, y, paint)
            }
        }
    }

    private fun getNextHeaderView(parent: RecyclerView, currentPosition: Int): View? {
    for (i in 1 until parent.childCount) {
        val child = parent.getChildAt(i)
        val position = parent.getChildAdapterPosition(child)
        if (position != RecyclerView.NO_POSITION) {
            val header = mListener.getHeaderForPosition(position)
            val currentHeader = mListener.getHeaderForPosition(currentPosition)
            if (header != currentHeader) {
                return child
            }
        }
    }
    return null
    }
}

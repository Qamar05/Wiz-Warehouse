package com.vms.wizwarehouse.inventory_out_history


import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R

class StickyHeaderDecoration(
    context: Context,
    private val listener: StickyHeaderInterface
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
            color = Color.GRAY // Customize if needed
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
    }

    interface StickyHeaderInterface {
        fun getHeaderForPosition(position: Int): String
        fun isHeader(position: Int): Boolean
        fun getHeaderView(headerText: String, parent: RecyclerView): View
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount == 0 || listener == null) return

        val leftMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            parent.context.resources.displayMetrics
        ).toInt()

        var previousHeader = ""
        var currentHeader: String?

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue

            currentHeader = listener?.getHeaderForPosition(position)
            if (previousHeader != currentHeader) {
                if (currentHeader != null) {
                    drawHeader(canvas, child, currentHeader, parent, leftMargin)
                    previousHeader = currentHeader
                }
            }
        }

        // Sticky header logic
        val topChild = parent.getChildAt(0)
        val topPosition = parent.getChildAdapterPosition(topChild)
        if (topPosition == RecyclerView.NO_POSITION) return

        val topHeader = listener?.getHeaderForPosition(topPosition)
        val headerView = listener?.getHeaderView(topHeader!!, parent) ?: return

        headerView.measure(
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        headerView.layout(0, 0, parent.width, headerView.measuredHeight)

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
        headerView.draw(canvas)
        canvas.restore()
    }

    private fun drawHeader(
        canvas: Canvas,
        child: View,
        headerText: String,
        parent: RecyclerView,
        top: Int
    ) {
        val headerView: View = listener.getHeaderView(headerText, parent)
            ?: return // Your method to get header view

        // Measure and layout the header view
        headerView.measure(
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        headerView.layout(0, 0, parent.width, headerView.measuredHeight)

        // Set start (left) margin here in pixels
        val startMarginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f, parent.context.resources.displayMetrics
        ).toInt()

        //        int topMarginPx = (int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP, 8, parent.getContext().getResources().getDisplayMetrics());

// Translate canvas before drawing (add both top and start margin)
        canvas.save()
        canvas.translate(startMarginPx.toFloat(), (child.top - headerView.height).toFloat())
        headerView.draw(canvas)
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION || listener == null) return

        // Check if current item is last in its section
        var isLastInSection = false

        val currentHeader: String = listener.getHeaderForPosition(position)
        val nextHeader =
            if (position + 1 < state.itemCount) listener.getHeaderForPosition(position + 1) else ""

        if (currentHeader != nextHeader) {
            // It's the last item in the section
            isLastInSection = true
        }

        if (isLastInSection) {
            // Convert 30dp to pixels
            val spaceInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                30f,
                parent.context.resources.displayMetrics
            ).toInt()
            outRect.bottom = spaceInPx
        } else {
            outRect.bottom = 0
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val itemCount = state.itemCount

        for (i in 0..<childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION || listener == null) continue

            val currentHeader: String = listener.getHeaderForPosition(position)
            val nextHeader =
                if (position + 1 < itemCount) listener.getHeaderForPosition(position + 1) else ""

            if (currentHeader != nextHeader) {
                // This is the last item in the section, draw dotted line
                val left = (child.left + child.paddingLeft).toFloat()
                val right = (child.right - child.paddingRight).toFloat()
                val y = (child.bottom + child.paddingBottom + 10).toFloat() // Adjust Y as needed

                canvas.drawLine(left, y, right, y, paint)
            }
        }
    }

    private fun getNextHeaderView(parent: RecyclerView, currentPosition: Int): View? {
        for (i in 1..<parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position != RecyclerView.NO_POSITION) {
                val header: String = listener.getHeaderForPosition(position)
                val currentHeader: String = listener.getHeaderForPosition(currentPosition)
                if (header != currentHeader) {
                    return child
                }
            }
        }
        return null
    }


}
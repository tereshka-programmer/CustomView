package com.example.fullcustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StyleRes
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

typealias OnCellActionListener = (row: Int, column: Int, field: TicTacToeField) -> Unit

class TicTacToeView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    var ticTacToeField: TicTacToeField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            value?.listeners?.add(listener)
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    var actionListener: OnCellActionListener? = null

    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    private val fieldRect = RectF(0f, 0f, 0f, 0f)
    private var cellSize: Float = 0f
    private var cellPadding: Float = 0f

    private val cellRect = RectF()

    private lateinit var player1Paint: Paint
    private lateinit var player2Paint: Paint
    private lateinit var gridPaint: Paint

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.DefaultTicTacToeFieldStyle)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.ticTacToeFieldStyle)
    constructor(context: Context) : this(context, null)

    init {
        if (attributeSet != null){
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        } else {
            initDefaultColors()
        }
        initPaints()
        if (isInEditMode) {
            ticTacToeField = TicTacToeField(8, 6)
            ticTacToeField?.setCell(4,2,Cell.PLAYER_1)
            ticTacToeField?.setCell(4,3,Cell.PLAYER_2)
        }
        isFocusable = true
        isClickable = true
    }

    private fun initPaints() {
        player1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        player1Paint.color = player1Color
        player1Paint.style = Paint.Style.STROKE
        player1Paint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)

        player2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        player2Paint.color = player2Color
        player2Paint.style = Paint.Style.STROKE
        player2Paint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)

        gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        gridPaint.color = gridColor
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ticTacToeField?.listeners?.add(listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ticTacToeField?.listeners?.remove(listener)
    }

    private fun initAttributes(attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int){
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TicTacToeView, defStyleAttr, defStyleRes)

        player1Color = typedArray.getColor(R.styleable.TicTacToeView_player1Color, PLAYER1_DEFAULT_COLOR)
        player2Color = typedArray.getColor(R.styleable.TicTacToeView_player2Color, PLAYER2_DEFAULT_COLOR)
        gridColor = typedArray.getColor(R.styleable.TicTacToeView_gridColor, GRID_DEFAULT_COLOR)

        typedArray.recycle()
    }

    private fun initDefaultColors() {
        player1Color = PLAYER1_DEFAULT_COLOR
        player2Color = PLAYER2_DEFAULT_COLOR
        gridColor = GRID_DEFAULT_COLOR
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val desiredCellSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DESIRED_CELL_SIZED, resources.displayMetrics).toInt()
        val rows = ticTacToeField?.rows ?: 0
        val columns = ticTacToeField?.columns ?: 0

        val desiredWidth = max(minWidth, columns * desiredCellSizeInPixels + paddingLeft + paddingRight)
        val desiredHeight = max(minHeight, rows * desiredCellSizeInPixels + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (ticTacToeField == null) return
        if (cellSize == 0f) return
        if (fieldRect.width() <= 0) return
        if (fieldRect.height() <= 0) return

        drawGrid(canvas)
        drawCells(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val field = this.ticTacToeField ?: return false
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                val row = getRow(event)
                val column = getColumn(event)
                if (row >= 0 && column >= 0 && row < field.rows && column < field.columns) {
                    actionListener?.invoke(row, column, field)
                    return true
                }
                return false
            }
        }
        return false
    }

    private fun getRow(event: MotionEvent): Int {
        return ((event.y - fieldRect.top) / cellSize).toInt()
    }

    private fun getColumn(event: MotionEvent): Int {
        return ((event.x - fieldRect.left) / cellSize).toInt()
    }

    private fun drawGrid(canvas: Canvas) {
        val field = this.ticTacToeField ?: return
        val xStart = fieldRect.left
        val xEnd = fieldRect.right
        for (i in 0..field.rows) {
            val y = fieldRect.top + cellSize * i
            canvas.drawLine(xStart, y, xEnd, y, gridPaint)
        }

        val yStart = fieldRect.top
        val yEnd = fieldRect.bottom
        for (i in 0..field.columns) {
            val x = fieldRect.left + cellSize * i
            canvas.drawLine(x, yStart, x, yEnd, gridPaint)
        }
    }

    private fun drawCells(canvas: Canvas) {
        val field = this.ticTacToeField ?: return
        for (row in 0 until field.rows) {
            for (column in 0 until field.columns){
                val cell = field.getCell(row, column)
                if (cell == Cell.PLAYER_1) {
                    drawPlayer1(canvas, row, column)
                } else if (cell == Cell.PLAYER_2){
                    drawPlayer2(canvas, row, column)
                }
            }
        }
    }

    private fun drawPlayer1(canvas: Canvas, row: Int, column: Int){
        val cellRect = getCellRect(row, column)
        canvas.drawLine(cellRect.left, cellRect.top, cellRect.right, cellRect.bottom, player1Paint)
        canvas.drawLine(cellRect.right, cellRect.top, cellRect.left, cellRect.bottom, player1Paint)
    }

    private fun drawPlayer2(canvas: Canvas, row: Int, column: Int){
        val cellRect = getCellRect(row, column)
        canvas.drawCircle(cellRect.centerX(), cellRect.centerY(), cellRect.width() / 2, player2Paint)
    }

    private fun getCellRect(row: Int, column: Int) : RectF{
        cellRect.left = fieldRect.left + column * cellSize + cellPadding
        cellRect.top = fieldRect.top + row * cellSize + cellPadding
        cellRect.right = cellRect.left + cellSize - cellPadding * 2
        cellRect.bottom = cellRect.top + cellSize - cellPadding * 2
        return cellRect
    }

    private fun updateViewSizes() {
        val field = this.ticTacToeField ?: return

        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingBottom - paddingTop

        val cellWidth = safeWidth / field.columns.toFloat()
        val cellHeight = safeHeight / field.rows.toFloat()

        cellSize = min(cellWidth, cellHeight)
        cellPadding = cellSize * 0.2f

        val fieldWidth = cellSize * field.columns
        val fieldHeight = cellSize * field.rows

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    private val listener: OnFieldChangedListener = {
        invalidate()
    }

    companion object {
        const val PLAYER1_DEFAULT_COLOR = Color.GREEN
        const val PLAYER2_DEFAULT_COLOR = Color.RED
        const val GRID_DEFAULT_COLOR = Color.GRAY

        const val DESIRED_CELL_SIZED = 50f
    }
}
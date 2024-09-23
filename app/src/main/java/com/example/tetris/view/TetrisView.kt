package com.example.tetris.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.example.tetris.GameActivity
import com.example.tetris.constants.CellConstants
import com.example.tetris.constants.FieldConstants
import com.example.tetris.models.AppModel
import com.example.tetris.models.Block


class TetrisView : View {
    private val paint = Paint()
    private var lastMove: Long = 0
    private var model: AppModel? = null
    private var activity: GameActivity? = null
    private val viewHandler = ViewHandler(this)
    private var cellSize: Dimension = Dimension(0, 0)
    private var frameOffset: Dimension = Dimension(0, 0)

    constructor (context: Context, attrs: AttributeSet) :
            super(context, attrs)

    constructor (context: Context, attrs: AttributeSet, defStyle: Int) :
            super(context, attrs, defStyle)

    companion object {
        private val DELAY = 500
        private val BLOCK_OFFSET = 2
        private val FRAМE_OFFSET_ВАSЕ = 10
    }

    fun setModel(model: AppModel) {
        this.model = model
    }

    fun setActivity(gameActivity: GameActivity) {
        this.activity = gameActivity
    }

    fun setGameCommnand(move: AppModel.Motions) {
        if (null != model && (model?.currentState == AppModel.Statuses.ACTIVE.name)) {
            if (AppModel.Motions.DOWN == move) {
                model?.generateField(move.name)
                invalidate()
                return
            }
            setGameCommandWithDelay(move)
        }
    }

    fun setGameCommandWithDelay(move: AppModel.Motions) {
        val now = System.currentTimeMillis()
        if (now - lastMove > DELAY) {
            model?.generateField(move.name)
            invalidate()
            lastMove = now
        }
        updateScores()
        viewHandler.sleep(DELAY.toLong())

    }

    private fun updateScores() {
        activity?.tvCurrentScore?.text = "${model?.score}"
        activity?.tvHighScore?.text = "${activity?.appPreferences?.getHighScore()}"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawFrame(canvas)
        if (model != null) {
            for (i in 0 until FieldConstants.ROW_COUNТ.value) {
                for (j in 0 until FieldConstants.COLUМN_COUNТ.value) {
                    drawCell(canvas, i, j)
                }
            }
        }
    }

    private fun drawFrame(canvas: Canvas) {
        paint.color = Color.LTGRAY
        canvas.drawRect(
            frameOffset.width.toFloat(),
            frameOffset.height.toFloat(),
            width - frameOffset.width.toFloat(),
            height - frameOffset.height.toFloat(), paint
        )
    }

    private fun drawCell(canvas: Canvas, row: Int, соl: Int) {
        val cellStatus = model?.getCellStatus(row, соl)
        if (CellConstants.EMPTY.value != cellStatus) {
            val color = if (CellConstants.EPHEМERAL.value == cellStatus) {
                model?.currentBlock?.color
            } else {
                Block.getColor(cellStatus as Byte)
            }
            drawCell(canvas, соl, row, color as Int)
        }
    }

    private fun drawCell(canvas: Canvas, х: Int, у: Int, rgbColor: Int) {
        paint.color = rgbColor
        val top: Float = (frameOffset.height + у * cellSize.height + BLOCK_OFFSET).toFloat()
        val left: Float = (frameOffset.width + х * cellSize.width + BLOCK_OFFSET).toFloat()
        val bottom: Float =
            (frameOffset.height + (у + 1) * cellSize.height - BLOCK_OFFSET).toFloat()
        val right: Float = (frameOffset.width + (х + 1) * cellSize.width - BLOCK_OFFSET).toFloat()
        val rectangle = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectangle, 4F, 4F, paint)
    }

    override fun onSizeChanged(width: Int, height: Int, previousWidth: Int, previousHeight: Int) {
        super.onSizeChanged(width, height, previousWidth, previousHeight)
        val cellWidth = (width - 2 * FRAМE_OFFSET_ВАSЕ) / FieldConstants.COLUМN_COUNТ.value
        val cellHeight = (height - 2 * FRAМE_OFFSET_ВАSЕ) / FieldConstants.ROW_COUNТ.value
        val n = Math.min(cellWidth, cellHeight)
        this.cellSize = Dimension(n, n)
        FieldConstants.ROW_COUNТ.value
        val offsetX = (width - FieldConstants.COLUМN_COUNТ.value * n) / 2
        val offsetY = (height - FieldConstants.ROW_COUNТ.value * n) / 2
        this.frameOffset = Dimension(offsetX, offsetY)


    }

    private class ViewHandler(private val owner: TetrisView) : Handler() {
        override fun handleMessage(message: Message) {
            if (message.what == 0) {
                if (owner.model != null) {
                    if (owner.model!!.isGameOver()) {
                        owner.model?.endGame()
                        Toast.makeText(
                            owner.activity, "Game over",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                    if (owner.model!!.isGameActive()) {
                        owner.setGameCommandWithDelay(AppModel.Motions.DOWN)
                    }
                }
            }
        }

        fun sleep(delay: Long) {
            this.removeMessages(0)
            this.sendMessageDelayed(this.obtainMessage(0), delay)
        }
    }

    private data class Dimension(val width: Int, val height: Int)
}
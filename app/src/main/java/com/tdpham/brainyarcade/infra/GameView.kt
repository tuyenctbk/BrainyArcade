package com.tdpham.brainyarcade.infra

import android.graphics.Canvas
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Common interface for all logic puzzle views.
 */
interface GameView {
    /**
     * Draw the game state on the canvas.
     */
    fun onDraw(canvas: Canvas)

    /**
     * Handle DPAD and keyboard events for Android TV.
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean

    /**
     * Handle touch events for Mobile/Tablet.
     */
    fun onTouchEvent(event: MotionEvent): Boolean

    /**
     * Reset the game to a new random state.
     * @param seed optional seed for deterministic generation (e.g. Daily Challenge)
     * @param level optional level for progressive difficulty
     */
    fun resetGame(seed: Long = -1, level: Int = 1)

    /**
     * Undo the last move (if supported).
     */
    fun undo() {}
}

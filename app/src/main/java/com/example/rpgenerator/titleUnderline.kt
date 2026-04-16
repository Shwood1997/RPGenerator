package com.example.rpgenerator

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.style.ReplacementSpan

// This file creates the fancy gold underline effect used in the app title.
class TitleUnderline (
    private val underlineHeight: Float, // How thick the line is
    private val goldColor: Int,         // The color of the line
    private val verticalOffset: Float   // How far below the text the line sits
) : ReplacementSpan() {

    // This part tells the app how much space the text needs
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
    }

    // This part actually draws the text and the fancy line on the screen
    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ){
        // First, draw the actual letters
        val width = paint.measureText(text, start, end)
        canvas.drawText(text, start, end, x, y.toFloat(), paint)

        // Create a "fading" effect so the line starts and ends transparently
        val gradient = LinearGradient(
            x,
            y + verticalOffset,
            x + width,
            y + verticalOffset,
            intArrayOf(0x00FFFFFF, goldColor, 0x00FFFFFF), // Transparent -> Gold -> Transparent
            floatArrayOf(0f, .5f, 1f),
            Shader.TileMode.CLAMP
        )

        // Prepare the "paint brush" for the line
        val underlinePaint = Paint(paint)
        underlinePaint.shader = gradient
        underlinePaint.strokeWidth = underlineHeight

        // Finally, draw the actual line under the text
        canvas.drawLine(
            x,
            y + verticalOffset,
            x + width,
            y + verticalOffset,
            underlinePaint
        )
    }
}

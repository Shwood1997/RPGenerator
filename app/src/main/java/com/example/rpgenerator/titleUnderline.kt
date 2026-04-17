package com.example.rpgenerator

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.style.ReplacementSpan

// This file creates the fancy gold underline effect used in the app title.
class TitleUnderline (
    // How thick the line is
    private val underlineHeight: Float, 
    // The color of the line
    private val goldColor: Int,         
    // How far below the text the line sits
    private val verticalOffset: Float   
) : ReplacementSpan() {

    // This part tells the app how much space the text needs
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // Measure the width of the text
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
        // First, measure how wide the text is
        val width = paint.measureText(text, start, end)
        // Draw the actual letters on the screen
        canvas.drawText(text, start, end, x, y.toFloat(), paint)

        // Create a fading color effect
        val gradient = LinearGradient(
            x,
            y + verticalOffset,
            x + width,
            y + verticalOffset,
            intArrayOf(0x00FFFFFF, goldColor, 0x00FFFFFF), 
            floatArrayOf(0f, .5f, 1f),
            Shader.TileMode.CLAMP
        )

        // Prepare the paint object for drawing the line
        val underlinePaint = Paint(paint)
        // Apply the fading color to the brush
        underlinePaint.shader = gradient
        // Set the thickness of the line
        underlinePaint.strokeWidth = underlineHeight

        // Draw the actual line under the text
        canvas.drawLine(
            x,
            y + verticalOffset,
            x + width,
            y + verticalOffset,
            underlinePaint
        )
    }
}

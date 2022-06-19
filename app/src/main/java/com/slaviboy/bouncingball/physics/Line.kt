package com.slaviboy.bouncingball.physics

import android.graphics.*
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Line class that is used to create lines which are draw on canvas, specified in the draw() method.
 * The two ends(pointers) of the line can have one of the next four tips: arrow, circle, rectangle
 * and diamond that can be specify using the type properties.
 */
class Line(

    // line properties
    var lineStrokeWidth: Float = 2.0f,
    var lineStrokeColor: Int = Color.parseColor("#0f0f0f"),
    var lineDashEffect: FloatArray = floatArrayOf(), // floatArrayOf(5f, 10f, 15f, 20f)

    // start pointer properties
    var startPoint: PointF = PointF(),
    var startPointerStrokeWidth: Float = 2.0f,
    var startPointerFillColor: Int = Color.parseColor("#489efa"),
    var startPointerStrokeColor: Int = Color.parseColor("#0f0f0f"),
    var startPointerType: Int = TYPE_ARROW,
    var startPointerDashPattern: FloatArray = floatArrayOf(),

    // end pointer properties
    var endPoint: PointF = PointF(),
    var endPointerStrokeWidth: Float = 3.0f,
    var endPointerFillColor: Int = Color.parseColor("#489efa"),
    var endPointerStrokeColor: Int = Color.parseColor("#0f0f0f"),
    var endPointerType: Int = TYPE_CIRCLE,
    var endPointerDashPattern: FloatArray = floatArrayOf(),

    // arrows size properties
    var startArrowLong: Float = 40.0f,
    var startArrowWide: Float = 40.0f,
    var startArrowFolded: Float = 30.0f,
    var endArrowLong: Float = 40.0f,
    var endArrowWide: Float = 40.0f,
    var endArrowFolded: Float = 30.0f,

    // circles size properties
    var startCircleRadius: Float = 10.0f,
    var endCircleRadius: Float = 10.0f,

    // rectangle size properties
    var startRectangleSide: Float = 40.0f,
    var endRectangleSide: Float = 10.0f,

    // diamonds size properties
    var startDiamondDiagonalLong: Float = 40.0f,
    var startDiamondDiagonalWide: Float = 40.0f,
    var endDiamondDiagonalLong: Float = 40.0f,
    var endDiamondDiagonalWide: Float = 40.0f

) {

    // public static values
    companion object {
        var TYPE_NONE: Int = 0
        var TYPE_ARROW: Int = 1
        var TYPE_CIRCLE: Int = 2
        var TYPE_RECTANGLE: Int = 3
        var TYPE_DIAMOND: Int = 4
    }

    /**
     * Method that draws current line, on a specific canvas.
     * @param canvas Canvas on which to draw the line
     */
    fun draw(canvas: Canvas) {

        val paint = Paint()
        paint.isAntiAlias = true

        val angle = atan2(
            (endPoint.y - startPoint.y).toDouble(),
            (endPoint.x - startPoint.x).toDouble()
        )

        val hyp: Float =
            sqrt(
                ((endPoint.x - startPoint.x) * (endPoint.x - startPoint.x) +
                        (endPoint.y - startPoint.y) * (endPoint.y - startPoint.y)).toDouble()
            )
                .toFloat()

        // translate to start as center
        canvas.save()
        canvas.translate(this.startPoint.x, this.startPoint.y)
        canvas.rotate(Math.toDegrees(angle).toFloat())

        // offset between start pointer and line
        val startOffset: Float = when (startPointerType) {
            TYPE_ARROW -> {
                startArrowFolded / 2
            }
            TYPE_CIRCLE -> {
                startCircleRadius
            }
            TYPE_RECTANGLE -> {
                startRectangleSide / 2
            }
            TYPE_DIAMOND -> {
                startDiamondDiagonalLong / 2
            }
            else -> {
                0.0f
            }
        }

        // offset between end pointer and line
        val endOffset: Float = when (endPointerType) {
            TYPE_ARROW -> {
                endArrowFolded / 2
            }
            TYPE_CIRCLE -> {
                endCircleRadius * 2
            }
            TYPE_RECTANGLE -> {
                endRectangleSide / 2
            }
            TYPE_DIAMOND -> {
                endDiamondDiagonalLong / 2
            }
            else -> {
                0.0f
            }
        }

        val width = if (hyp > startOffset) {
            hyp
        } else {
            startOffset
        }

        // draw line
        val linePath = Path()
        linePath.moveTo(startOffset, 0.0f)
        linePath.lineTo(width - endOffset, 0.0f)
        paint.strokeWidth = lineStrokeWidth
        paint.style = Paint.Style.STROKE
        paint.color = lineStrokeColor
        paint.pathEffect = if (lineDashEffect.isNotEmpty()) {
            DashPathEffect(lineDashEffect, 0.0f)
        } else {
            null
        }
        canvas.drawPath(linePath, paint)

        // draw start pointer
        when (startPointerType) {
            TYPE_ARROW -> {
                val startPointerPath: Path = createArrowPath(
                    arrayOf(
                        PointF(-startArrowFolded / 2, 0.0f),
                        PointF(startArrowLong - startArrowFolded / 2, -startArrowWide / 2),
                        PointF(startArrowFolded / 2, 0.0f),
                        PointF(startArrowLong - startArrowFolded / 2, startArrowWide / 2)
                    )
                )

                // stroke
                setStroke(
                    paint,
                    startPointerStrokeWidth,
                    startPointerStrokeColor,
                    startPointerDashPattern
                )
                canvas.drawPath(startPointerPath, paint)

                // fill
                setFill(paint, startPointerFillColor)
                canvas.drawPath(startPointerPath, paint)
            }

            TYPE_CIRCLE -> {

                // stroke
                setStroke(
                    paint,
                    startPointerStrokeWidth,
                    startPointerStrokeColor,
                    startPointerDashPattern
                )
                canvas.drawCircle(0.0f, 0.0f, startCircleRadius, paint)

                // fill
                setFill(paint, startPointerFillColor)
                canvas.drawCircle(0.0f, 0.0f, startCircleRadius, paint)

            }

            TYPE_RECTANGLE -> {

                // stroke
                setStroke(
                    paint,
                    startPointerStrokeWidth,
                    startPointerStrokeColor,
                    startPointerDashPattern
                )
                canvas.drawRect(
                    -startRectangleSide / 2,
                    -startRectangleSide / 2,
                    startRectangleSide / 2,
                    startRectangleSide / 2,
                    paint
                )

                // fill
                setFill(paint, startPointerFillColor)
                canvas.drawRect(
                    -startRectangleSide / 2,
                    -startRectangleSide / 2,
                    startRectangleSide / 2,
                    startRectangleSide / 2,
                    paint
                )
            }

            TYPE_DIAMOND -> {

                val startPointerPath: Path = createArrowPath(
                    arrayOf(
                        PointF(-startDiamondDiagonalLong / 2, 0.0f),
                        PointF(0.0f, -startDiamondDiagonalWide / 2),
                        PointF(startDiamondDiagonalLong / 2, 0.0f),
                        PointF(0.0f, startDiamondDiagonalWide / 2)
                    )
                )

                // stroke
                setStroke(
                    paint,
                    startPointerStrokeWidth,
                    startPointerStrokeColor,
                    startPointerDashPattern
                )
                canvas.drawPath(startPointerPath, paint)

                // fill
                setFill(paint, startPointerFillColor)
                canvas.drawPath(startPointerPath, paint)
            }
        }

        // draw end pointer
        when (endPointerType) {
            TYPE_ARROW -> {

                val endPointerPath: Path = createArrowPath(
                    arrayOf(
                        PointF(hyp + endArrowFolded / 2, 0.0f),
                        PointF(hyp - (endArrowLong - endArrowFolded / 2), -endArrowWide / 2),
                        PointF(hyp - endArrowFolded / 2, 0.0f),
                        PointF(hyp - (endArrowLong - endArrowFolded / 2), endArrowWide / 2)
                    )
                )

                // stroke
                setStroke(
                    paint,
                    endPointerStrokeWidth,
                    endPointerStrokeColor,
                    endPointerDashPattern
                )
                canvas.drawPath(endPointerPath, paint)

                // fill
                setFill(paint, endPointerFillColor)
                canvas.drawPath(endPointerPath, paint)

            }

            TYPE_CIRCLE -> {

                // stroke
                setStroke(
                    paint,
                    endPointerStrokeWidth,
                    endPointerStrokeColor,
                    endPointerDashPattern
                )
                canvas.drawCircle(hyp - endCircleRadius, 0.0f, endCircleRadius, paint)

                // fill
                setFill(paint, endPointerFillColor)
                canvas.drawCircle(hyp - endCircleRadius, 0.0f, endCircleRadius, paint)

            }

            TYPE_RECTANGLE -> {

                // stroke
                setStroke(
                    paint,
                    endPointerStrokeWidth,
                    endPointerStrokeColor,
                    endPointerDashPattern
                )
                canvas.drawRect(
                    hyp - endRectangleSide / 2,
                    -endRectangleSide / 2,
                    hyp + endRectangleSide / 2,
                    endRectangleSide / 2,
                    paint
                )

                // fill
                setFill(paint, endPointerFillColor)
                canvas.drawRect(
                    hyp - endRectangleSide / 2,
                    -endRectangleSide / 2,
                    hyp + endRectangleSide / 2,
                    endRectangleSide / 2,
                    paint
                )
            }

            TYPE_DIAMOND -> {

                val endPointerPath: Path = createArrowPath(
                    arrayOf(
                        PointF(hyp - endDiamondDiagonalLong / 2, 0.0f),
                        PointF(hyp, -endDiamondDiagonalWide / 2),
                        PointF(hyp + endDiamondDiagonalLong / 2, 0.0f),
                        PointF(hyp, endDiamondDiagonalWide / 2)
                    )
                )

                // stroke
                setStroke(
                    paint,
                    endPointerStrokeWidth,
                    endPointerStrokeColor,
                    endPointerDashPattern
                )
                canvas.drawPath(endPointerPath, paint)

                // fill
                setFill(paint, endPointerFillColor)
                canvas.drawPath(endPointerPath, paint)
            }
        }

        canvas.restore()
    }


    /**
     * Create arrow path that is made of all points presented in the array
     * @param points Point array
     * @param isClosed Specify if first and last points should be connected
     */
    private fun createArrowPath(points: Array<PointF>, isClosed: Boolean = true): Path {
        val path = Path()
        path.moveTo(points[0].x, points[0].y)

        for (i in points.indices) {
            path.lineTo(points[i].x, points[i].y)
        }

        if (isClosed) {
            path.close()
        }
        return path
    }


    /**
     * Set stroke properties, to paint object, that includes the properties: stroke width, color and dash pattern
     * @param paint Paint object
     * @param strokeWidth Stroke width
     * @param strokeColor Stroke color
     * @param dashPattern Dash pattern float array
     */
    private fun setStroke(
        paint: Paint,
        strokeWidth: Float,
        strokeColor: Int,
        dashPattern: FloatArray
    ) {
        // stroke
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.color = strokeColor

        paint.pathEffect = if (dashPattern.isNotEmpty()) {
            DashPathEffect(dashPattern, 0.0f)
        } else {
            null
        }
    }

    /**
     * Set fill properties, to paint object, that includes the property: fill color
     * @param paint Paint object
     * @param fillColor Fill color
     */
    private fun setFill(paint: Paint, fillColor: Int) {
        // fill
        paint.style = Paint.Style.FILL
        paint.color = fillColor
    }
}
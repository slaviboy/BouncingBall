package com.slaviboy.bouncingball.physics

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

class CanvasView : SurfaceView, SurfaceHolder.Callback, View.OnTouchListener, Runnable {

    constructor(context: Context?) : super(context) {
        setBitmaps(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setBitmaps(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setBitmaps(context)
    }

    private var ballBitmap: Bitmap?             // bitmap with the ball image
    private val fps: Int                        // fps
    private val allowedNumberOfFingers: Int     // allowed number of fingers on the screen
    private var mHolder: SurfaceHolder?         // surface holder object
    private var drawThread: Thread?             // the drawing thread
    private var surfaceReady: Boolean           // if surface is ready for drawing
    private var drawingActive: Boolean          // if drawing is active
    private var paint: Paint                    // paint object object for the drawing
    private var balls: ArrayList<Ball>          // array with all the balls that are created
    private var environment: Environment        // environment of where the ball are
    private var activePointers: Array<PointF?>  // active pointers, user finger at the screen at the moment
    private var activeLines: Array<Line?>       // active lines for the velocity vector
    private var activeBalls: Array<Ball?>       // active ball, that are about to be created, when the user remove finger from screen
    private var mBackgroundColor: Int           // background color for the surface view

    init {
        ballBitmap = null
        fps = 60
        allowedNumberOfFingers = 2
        mHolder = null
        drawThread = null
        surfaceReady = false
        drawingActive = false
        balls = ArrayList()
        environment = Environment()
        activePointers = arrayOfNulls(allowedNumberOfFingers)
        activeLines = arrayOfNulls(allowedNumberOfFingers)
        activeBalls = arrayOfNulls(allowedNumberOfFingers)
        mBackgroundColor = Color.parseColor("#000000")

        paint = Paint()
        paint.isAntiAlias = true

        holder.addCallback(this)
        setOnTouchListener(this)
    }

    private fun setBitmaps(context: Context?) {

        // load ball bitmap
        if (context != null) {
            ballBitmap = Ball.getBitmapFromAsset(context, "img/ball2.png")
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (width == 0 || height == 0) {
            return
        }
        // resize UI here
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // surface is not used anymore - stop the drawing thread
        stopDrawThread()

        // release the surface
        mHolder?.surface?.release()

        mHolder = null
        surfaceReady = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.mHolder = holder

        if (drawThread != null) {
            drawingActive = false
            try {
                drawThread?.join()
            } catch (e: InterruptedException) {
                // do nothing
            }

        }

        surfaceReady = true
        startDrawThread()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        // get pointer index from the event object
        val pointerIndex = event.actionIndex

        // get pointer ID
        val pointerId = event.getPointerId(pointerIndex)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {

                if (pointerId < allowedNumberOfFingers) {

                    // set the start pointer position
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)
                    activePointers[pointerId] = PointF(x, y)
                    activeLines[pointerId] =
                        Line(
                            startPoint = PointF(x, y),
                            endPoint = PointF(x, y),
                            startPointerStrokeColor = Color.parseColor("#00ffffff"),
                            endPointerStrokeColor = Color.parseColor("#00ffffff"),
                            startPointerFillColor = Color.parseColor("#ffffff"),
                            endPointerFillColor = Color.parseColor("#ffffff"),
                            lineStrokeColor = Color.parseColor("#ffffff")
                        )

                    // add new ball
                    balls.add(Ball(x = x, y = y, bitmap = ballBitmap, radius = 150.0f))
                    activeBalls[pointerId] = balls[balls.size - 1]
                }
            }

            MotionEvent.ACTION_MOVE -> {

                val pointerCount = event.pointerCount
                for (i in 0 until pointerCount) {
                    val id = event.getPointerId(i)
                    if (id < allowedNumberOfFingers) {

                        val x = event.getX(i)
                        val y = event.getY(i)

                        // change current pointer position
                        val point: PointF? = activePointers[id]
                        if (point != null) {
                            point.x = x
                            point.y = y
                        }

                        // set the end point
                        val line: Line? = activeLines[id]
                        if (line != null) {
                            line.endPoint = PointF(x, y)
                        }
                    }
                }

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (pointerId < allowedNumberOfFingers) {

                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)

                    val ball: Ball? = activeBalls[pointerId]
                    if (ball != null) {
                        ball.velocity.x = (ball.x - x) / 10
                        ball.velocity.y = (ball.y - y) / 10
                        ball.isActive = true
                        ball.isVisible = true
                    }

                    activePointers[pointerId] = null
                    activeLines[pointerId] = null
                    activeBalls[pointerId] = null
                }
            }
        }

        return true
    }

    /**
     * Method that is used as runnale for the drawing thread.
     */
    override fun run() {
        var frameStartTime: Long
        var frameTime: Long

        try {
            while (drawingActive) {
                if (holder == null) {
                    return
                }

                frameStartTime = System.nanoTime()
                val canvas = holder.lockCanvas()
                if (canvas != null) {

                    try {
                        // move, draw and collide balls
                        this.loop(canvas)
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }

                // calculate the time required to draw the frame in ms
                frameTime = (System.nanoTime() - frameStartTime) / 1000000


                val maxFrameTime = (1000.0 / fps).toInt()

                // faster than the max fps - limit the FPS
                if (frameTime < maxFrameTime) {
                    try {
                        Thread.sleep(maxFrameTime - frameTime)
                    } catch (e: InterruptedException) {
                        // ignore
                    }

                }
            }
        } catch (e: Exception) {
            Log.i("error", "Exception while locking/unlocking $e")
        }

    }

    /**
     * Stops the drawing thread.
     */
    private fun stopDrawThread() {
        if (drawThread == null) {
            return
        }
        drawingActive = false
        while (true) {
            try {
                drawThread?.join(5000)
                break
            } catch (e: Exception) {
                Log.i("error", "Could not join with draw thread $e")
            }

        }
        drawThread = null
    }

    /**
     * Creates and start new thread for drawing.
     */
    private fun startDrawThread() {
        if (surfaceReady && drawThread == null) {
            drawThread = Thread(this, "Draw thread")
            drawingActive = true
            drawThread?.start()
        }
    }

    /**
     * Method that is responsible for drawing, moving and colliding all the available balls,
     * and applying the physics that take place.
     */
    private fun loop(canvas: Canvas) {

        // fill the screen using background color
        canvas.drawColor(mBackgroundColor)

        // draw lines showing the velocity vectors
        for (line: Line? in activeLines) {
            line?.draw(canvas)
        }

        for (i: Int in balls.indices) {

            val ball = balls[i]
            if (ball.isActive) {


                // handling the ball collisions
                ball.collisionBalls(balls)
                ball.collisionWalls(width, height)

                ball.move(environment)
            }

            if (ball.isVisible) {
                ball.draw(canvas)
            }
        }
    }
}
package com.slaviboy.bouncingball.physics

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import java.io.IOException
import java.io.InputStream

/**
 * Ball class, that represent ball with custom properties like mass, elasticity, radius and position.
 * A bitmap or color, can be set as background for the ball.
 * @param x coordinate of ball center, unit:m(meter)
 * @param y coordinate of ball center, unit:m(meter)
 * @param radius ball radius, unit:m(meter)
 * @param elasticity ball elasticity
 * @param mass ball mass, unit:kg(kilogram)
 * @param isVisible whether ball should be drawn
 * @param isActive whether ball is active and should be used in collisions
 * @param bitmap bitmap for the ball background
 * @param color default color for the ball background
 * @param drawShadow whether overall shadow should be drawn
 * @param shadow default shadow radial gradient
 */
class Ball(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var radius: Float = 100.0f,
    var elasticity: Float = -0.8f,
    var mass: Float = 70.0f,
    var isVisible: Boolean = false,
    var isActive: Boolean = false,
    var bitmap: Bitmap? = null,
    var color: Int = Color.GREEN,
    var drawShadow: Boolean = true,
    var shadow: Shader = RadialGradient(
        4.0f, -6.0f, radius,
        intArrayOf(
            Color.parseColor("#3affdeff"),
            Color.parseColor("#23000000"),
            Color.parseColor("#ff000000")
        ),
        floatArrayOf(0.0f, 0.6f, 1.0f), Shader.TileMode.CLAMP
    )
) {

    companion object {

        /**
         * Return bitmap from image positioned in the assets folder
         */
        fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {

            val assetManager: AssetManager = context.assets
            val inputStream: InputStream
            var bitmap: Bitmap? = null
            try {
                inputStream = assetManager.open(filePath!!)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                // handle exception
                Log.i("error", "Could not load bitmap: $e")
            }
            return bitmap
        }

        var idCounter: Int = 0
    }

    var velocity: PointF = PointF()                             // velocity of the ball, unit: m/s (meter per seconds)
    var area: Double = (Math.PI * radius * radius) / 10000.0    // ball area, unit: m^2 (square meter)
    var angle: Float = 0.0f                                     // angle used for rotation, unit: radians
    var id: Int

    init {
        id = idCounter
        idCounter++
    }

    /**
     * Draw the ball on canvas, use color or bitmap depending, whether bitmap is available.
     * @param canvas canvas where the ball should be drawn
     */
    fun draw(canvas: Canvas) {

        canvas.save()
        canvas.translate(x, y)

        canvas.save()
        canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat())

        val paint = Paint()
        paint.isAntiAlias = true

        val localBmp = bitmap
        if (localBmp != null) {

            // draw bitmap
            val r = radius.toInt()
            canvas.drawBitmap(
                localBmp,
                Rect(0, 0, localBmp.width, localBmp.height),
                Rect(-r, -r, r, r),
                paint
            )
        } else {
            // draw color
            paint.color = color
            canvas.drawCircle(0.0f, 0.0f, radius + 5, paint)
        }
        canvas.restore()

        if (drawShadow) {
            paint.shader = shadow
            canvas.drawCircle(0.0f, 0.0f, radius + 5, paint)
        }
        canvas.restore()
    }

    /**
     * Move the ball by applying different forces like drag and gravity.
     * More info about the formulas : https://en.wikipedia.org/wiki/Bouncing_ball
     * @param drag frag coefficient of a sphere
     * @param density density of air 15°C
     * @param gravity 1G (gravity on earth)
     * @param ag gravitational acceleration
     * @param fps refresh rate
     */
    fun move(
        drag: Float = 0.47f,
        density: Float = 1.22f,
        gravity: Float = 1.0f,
        ag: Float = 9.81f,
        fps: Float = 1 / 60.0f
    ) {

        // drag forces: -0.5 * Cd * A * v^2 * rho
        var fx: Double = -0.5 * drag * density * area * velocity.x * velocity.x * (velocity.x / Math.abs(velocity.x))
        var fy: Double = -0.5 * drag * density * area * velocity.y * velocity.y * (velocity.y / Math.abs(velocity.y))

        fx = if (fx.isNaN()) 0.0 else fx
        fy = if (fy.isNaN()) 0.0 else fy

        // acceleration of the ball: F = ma and a = F/m
        val ax = fx / mass
        val ay = (ag * gravity) + (fy / mass)

        // ball velocity
        velocity.x += (ax * fps).toFloat()
        velocity.y += (ay * fps).toFloat()

        // ball position
        x += (velocity.x * fps * 100)
        y += (velocity.y * fps * 100)

        // ball rotational angle
        angle += velocity.x / radius
    }

    /**
     * Move the ball by applying the environment properties the ball is in.
     * @param environment object with properties about the environment
     */
    fun move(environment: Environment) {
        move(
            environment.dragCoefficient,
            environment.fluidDensity,
            environment.gravity,
            environment.gravitationalAcceleration,
            environment.refreshRate
        )
    }

    /**
     * Check for collisions with the other balls in the array.
     * @param balls array with balls
     */
    fun collisionBalls(balls: ArrayList<Ball>) {

        val b1 = this
        for (i: Int in 0 until balls.size) {
            val b2 = balls[i]
            if (b1.x != b2.x && b1.y != b2.y) {

                // quick check for potential collisions using AABBs
                if (b1.x + b1.radius + b2.radius > b2.x
                    && b1.x < b2.x + b1.radius + b2.radius
                    && b1.y + b1.radius + b2.radius > b2.y
                    && b1.y < b2.y + b1.radius + b2.radius
                ) {

                    // pythagoras
                    val distX = b1.x - b2.x
                    val distY = b1.y - b2.y
                    val d = Math.sqrt(((distX) * (distX) + (distY) * (distY)).toDouble()).toFloat()

                    // checking circle vs circle collision
                    if (d < b1.radius + b2.radius) {
                        val nx = (b2.x - b1.x) / d
                        val ny = (b2.y - b1.y) / d
                        val p =
                            2 * (b1.velocity.x * nx + b1.velocity.y * ny - b2.velocity.x * nx - b2.velocity.y * ny) / (b1.mass + b2.mass)

                        // point of collision
                        val colPointX =
                            ((b1.x * b2.radius) + (b2.x * b1.radius)) / (b1.radius + b2.radius)
                        val colPointY =
                            ((b1.y * b2.radius) + (b2.y * b1.radius)) / (b1.radius + b2.radius)

                        // stop overlap
                        b1.x = colPointX + b1.radius * (b1.x - b2.x) / d
                        b1.y = colPointY + b1.radius * (b1.y - b2.y) / d
                        b2.x = colPointX + b2.radius * (b2.x - b1.x) / d
                        b2.y = colPointY + b2.radius * (b2.y - b1.y) / d

                        // updating velocity to reflect collision
                        val b1VelocityX = p * b1.mass * nx
                        val b1VelocityY = p * b1.mass * ny
                        val b2VelocityX = p * b2.mass * nx
                        val b2VelocityY = p * b2.mass * ny

                        b1.velocity.x -= b1VelocityX
                        b1.velocity.y -= b1VelocityY
                        b2.velocity.x += b2VelocityX
                        b2.velocity.y += b2VelocityY
                    }
                }
            }
        }
    }

    /**
     * Check for ball collision with the four walls, surrounding the
     * current view size.
     * @param viewWidth view width
     * @param viewHeight view height
     */
    fun collisionWalls(viewWidth: Int, viewHeight: Int) {

        // right wall
        if (x > viewWidth - radius) {
            velocity.x *= elasticity
            x = viewWidth - radius
        }

        // left wall
        if (x < radius) {
            velocity.x *= elasticity
            x = radius
        }

        // bottom wall
        if (y > viewHeight - radius) {
            velocity.y *= elasticity
            y = viewHeight - radius
        }

        // top wall
        if (y < radius) {
            velocity.y *= elasticity
            y = radius
        }
    }
}
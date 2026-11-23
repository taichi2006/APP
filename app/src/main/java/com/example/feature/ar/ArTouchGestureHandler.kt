package com.example.feature.ar

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Xử lý cử chỉ chạm đa điểm cho AR/3D:
 * - 1 ngón: Xoay ngang 360° (Yaw) và cúi/ngửa (Pitch)
 * - 2 ngón: Thu phóng (Pinch zoom) và di chuyển vị trí (Pan X/Y)
 */
class ArTouchGestureHandler(
    context: Context,
    private val onTransformChanged: (yaw: Float, pitch: Float, scale: Float, posX: Float, posY: Float, posZ: Float) -> Unit
) : View.OnTouchListener {

    var currentYaw = 0f
        private set
    var currentPitch = 15f
        private set
    var currentScale = 1.0f
        private set
    var currentPosX = 0.0f
        private set
    var currentPosY = -0.4f
        private set
    var currentPosZ = -1.0f
        private set

    private var previousX = 0f
    private var previousY = 0f
    private var prevMidX = 0f
    private var prevMidY = 0f
    private var isTwoFingerGesture = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            currentScale *= detector.scaleFactor
            currentScale = currentScale.coerceIn(0.2f, 5.0f)
            notifyTransform()
            return true
        }
    })

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.pointerCount) {
            1 -> {
                isTwoFingerGesture = false
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        previousX = event.x
                        previousY = event.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!scaleDetector.isInProgress) {
                            val deltaX = event.x - previousX
                            val deltaY = event.y - previousY
                            previousX = event.x
                            previousY = event.y

                            currentYaw += deltaX * 0.4f
                            currentPitch += deltaY * 0.4f
                            currentPitch = currentPitch.coerceIn(-75f, 75f)

                            notifyTransform()
                        }
                        return true
                    }
                    else -> return true
                }
            }
            2 -> {
                val midX = (event.getX(0) + event.getX(1)) / 2f
                val midY = (event.getY(0) + event.getY(1)) / 2f
                when (event.actionMasked) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        prevMidX = midX
                        prevMidY = midY
                        isTwoFingerGesture = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isTwoFingerGesture && !scaleDetector.isInProgress) {
                            val deltaMidX = midX - prevMidX
                            val deltaMidY = midY - prevMidY
                            currentPosX += deltaMidX * 0.0015f
                            currentPosY -= deltaMidY * 0.0015f
                            notifyTransform()
                        }
                        prevMidX = midX
                        prevMidY = midY
                        return true
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        isTwoFingerGesture = false
                        return true
                    }
                    else -> return true
                }
            }
            else -> return true
        }
    }

    fun setInitialPosY(posY: Float) {
        currentPosY = posY
        notifyTransform()
    }

    fun reset(defaultPosY: Float = -0.4f) {
        currentYaw = 0f
        currentPitch = 15f
        currentScale = 1.0f
        currentPosX = 0.0f
        currentPosY = defaultPosY
        currentPosZ = -1.0f
        notifyTransform()
    }

    private fun notifyTransform() {
        onTransformChanged(currentYaw, currentPitch, currentScale, currentPosX, currentPosY, currentPosZ)
    }
}

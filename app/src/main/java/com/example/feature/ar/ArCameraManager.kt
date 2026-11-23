package com.example.feature.ar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.view.Surface
import android.view.SurfaceView
import androidx.core.content.ContextCompat

/**
 * Quản lý vòng đời Camera2 cho chế độ AR/VR trong ARoom
 */
class ArCameraManager(private val context: Context) {

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    val isRunning: Boolean
        get() = cameraDevice != null

    fun startCamera(
        surfaceView: SurfaceView,
        onSessionStarted: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            onError?.invoke("Chưa được cấp quyền Camera")
            return
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        if (cameraManager == null) {
            onError?.invoke("Không tìm thấy dịch vụ Camera")
            return
        }

        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId == null) {
                onError?.invoke("Không tìm thấy Camera phù hợp")
                return
            }

            val surface = surfaceView.holder.surface
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    if (surface != null && surface.isValid) {
                        createPreviewSession(camera, surface, onSessionStarted)
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    onError?.invoke("Lỗi Camera: $error")
                }
            }, null)
        } catch (e: Exception) {
            onError?.invoke(e.message ?: "Lỗi mở Camera")
        }
    }

    private fun createPreviewSession(
        camera: CameraDevice,
        surface: Surface,
        onSessionStarted: (() -> Unit)?
    ) {
        try {
            val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }
            camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return
                    captureSession = session
                    try {
                        session.setRepeatingRequest(builder.build(), null, null)
                        onSessionStarted?.invoke()
                    } catch (_: Exception) {}
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, null)
        } catch (_: Exception) {}
    }

    fun stopCamera() {
        try {
            captureSession?.stopRepeating()
            captureSession?.close()
            captureSession = null
        } catch (_: Exception) {}
        try {
            cameraDevice?.close()
            cameraDevice = null
        } catch (_: Exception) {}
    }
}

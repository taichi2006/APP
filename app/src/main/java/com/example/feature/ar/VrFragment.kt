package com.example.feature.ar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroom.R
import com.example.feature.product.ColorsAdapter
import com.example.feature.product.ProductDetailsFragmentArgs
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.gorisse.thomas.sceneform.scene.await

/**
 * Màn hình trải nghiệm 3D và Camera AR cho nội thất ARoom
 * Được tái cấu trúc theo mô đun:
 * - [ArCameraManager]: Quản lý Camera2 & preview
 * - [ArTouchGestureHandler]: Xử lý chạm đa điểm, xoay 360°, ngửa cúi và thu phóng
 * - [ArRoomStudioBuilder]: Dựng phòng 3D trực quan (sàn gỗ sồi, thảm, tường)
 */
class VrFragment : Fragment(R.layout.fragment_vr) {

    private val args by navArgs<ProductDetailsFragmentArgs>()

    private var sceneView3D: SceneView? = null
    private var cameraSurfaceView: SurfaceView? = null
    private var model3DNode: Node? = null
    private var model: Renderable? = null
    private var roomStudioNode: Node? = null

    private var btnSwitch: Button? = null
    private var tvInstruction: TextView? = null

    // Quản lý Camera2 và cử chỉ chạm
    private lateinit var cameraManager: ArCameraManager
    private lateinit var gestureHandler: ArTouchGestureHandler
    private var isCameraMode = false

    private var currentColor: Int = 0
    private var modelBaseScale = 1.0f

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableCameraMode(true)
        } else {
            Toast.makeText(requireContext(), "Cần cấp quyền Camera để xem không gian thực tế", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product
        val modelUri = product.model
        currentColor = arguments?.getInt("selectedColor", 0) ?: 0

        cameraManager = ArCameraManager(requireContext())

        sceneView3D = view.findViewById(R.id.sceneView3D)
        cameraSurfaceView = view.findViewById(R.id.cameraSurfaceView)
        val btnBack = view.findViewById<ImageView>(R.id.btnBackVr)
        val btnReset = view.findViewById<ImageView>(R.id.btnResetView)
        btnSwitch = view.findViewById(R.id.btnSwitchMode)
        val tvTitle = view.findViewById<TextView>(R.id.tvVrTitle)
        tvInstruction = view.findViewById(R.id.tvInstruction)
        val rvVrColors = view.findViewById<RecyclerView>(R.id.rvVrColors)

        tvTitle.text = product.name
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnReset.setOnClickListener {
            val box = model?.collisionShape as? com.google.ar.sceneform.collision.Box
            val defaultY = if (box != null && modelBaseScale > 0.001f) {
                -0.3f - (box.center.y * modelBaseScale)
            } else {
                -0.4f
            }
            gestureHandler.reset(defaultY)
            Toast.makeText(context, "Đã đặt lại góc nhìn", Toast.LENGTH_SHORT).show()
        }

        btnSwitch?.setOnClickListener {
            val targetMode = !isCameraMode
            if (targetMode) {
                val ctx = context ?: return@setOnClickListener
                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    enableCameraMode(true)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            } else {
                enableCameraMode(false)
            }
        }

        // Cấu hình đổi màu sắc mô hình trực tiếp trong màn hình 3D
        product.colors?.let { colors ->
            if (colors.isNotEmpty()) {
                rvVrColors.visibility = View.VISIBLE
                val colorsAdapter = ColorsAdapter()
                rvVrColors.adapter = colorsAdapter
                rvVrColors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                colorsAdapter.differ.submitList(colors)

                colorsAdapter.onItemClick = { color ->
                    currentColor = color
                    applyColorToModel(color)
                }
            }
        }

        cameraSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (isCameraMode && !cameraManager.isRunning) {
                    startCamera()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraManager.stopCamera()
            }
        })

        // Khởi tạo bộ xử lý cử chỉ chạm đa điểm
        gestureHandler = ArTouchGestureHandler(requireContext()) { yaw, pitch, scale, posX, posY, posZ ->
            applyModelTransform(yaw, pitch, scale, posX, posY, posZ)
        }
        sceneView3D?.setOnTouchListener(gestureHandler)

        applyShowroomClearColor()

        lifecycleScope.launchWhenCreated {
            loadModel(modelUri)
        }
    }

    private fun enableCameraMode(enable: Boolean) {
        isCameraMode = enable
        if (enable) {
            cameraSurfaceView?.visibility = View.VISIBLE
            makeSceneViewTransparent(true)
            roomStudioNode?.isEnabled = false
            startCamera()
            btnSwitch?.text = "Tắt Camera (Xem 3D)"
            tvInstruction?.text = "Camera AR: 1 ngón xoay/cúi • 2 ngón thu phóng/di chuyển"
        } else {
            cameraManager.stopCamera()
            cameraSurfaceView?.visibility = View.GONE
            makeSceneViewTransparent(false)
            roomStudioNode?.isEnabled = true
            btnSwitch?.text = "Xem qua Camera"
            tvInstruction?.text = "1 ngón xoay 360° & nhìn từ trên xuống • 2 ngón thu phóng"
        }
    }

    private fun makeSceneViewTransparent(transparent: Boolean) {
        sceneView3D?.let { sv ->
            try {
                if (transparent) {
                    sv.setZOrderMediaOverlay(true)
                    sv.holder.setFormat(PixelFormat.TRANSLUCENT)
                    sv.renderer?.filamentView?.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
                } else {
                    sv.setZOrderMediaOverlay(false)
                    sv.holder.setFormat(PixelFormat.OPAQUE)
                    sv.renderer?.filamentView?.blendMode = com.google.android.filament.View.BlendMode.OPAQUE
                    applyShowroomClearColor()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyShowroomClearColor() {
        try {
            sceneView3D?.renderer?.setClearColor(Color(0.94f, 0.95f, 0.97f, 1.0f))
        } catch (_: Exception) {}
    }

    private fun startCamera() {
        val sv = cameraSurfaceView ?: return
        cameraManager.startCamera(
            surfaceView = sv,
            onError = { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private suspend fun loadModel(modelUri: String?) {
        try {
            val ctx = context ?: return
            val uri = when {
                modelUri.isNullOrBlank() || modelUri == "arm_chair__furniture.glb" -> {
                    Uri.parse("models/chair_armchair.glb")
                }
                modelUri.startsWith("http://") || modelUri.startsWith("https://") -> {
                    Uri.parse(modelUri)
                }
                modelUri.startsWith("models/") -> {
                    Uri.parse(modelUri)
                }
                else -> {
                    Uri.parse("models/$modelUri")
                }
            }

            model = try {
                ModelRenderable.builder()
                    .setSource(ctx, uri)
                    .setIsFilamentGltf(true)
                    .await()
            } catch (e: Exception) {
                ModelRenderable.builder()
                    .setSource(ctx, Uri.parse("models/chair_armchair.glb"))
                    .setIsFilamentGltf(true)
                    .await()
            }

            displayIn3DScene()
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể tải mô hình: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayIn3DScene() {
        val renderable = model ?: return
        val scene = sceneView3D?.scene ?: return

        model3DNode?.let { scene.removeChild(it) }

        val box = renderable.collisionShape as? com.google.ar.sceneform.collision.Box
        var baseScale = 1.0f
        var yOffset = -0.4f
        var floorLevelY = -0.4f

        if (box != null) {
            val size = box.size
            val maxDim = maxOf(size.x, size.y, size.z)
            if (maxDim > 0.05f) {
                baseScale = 0.85f / maxDim
                val centerY = box.center.y
                yOffset = -0.3f - (centerY * baseScale)
                floorLevelY = yOffset + (box.center.y - box.size.y * 0.5f) * baseScale
            }
        }
        modelBaseScale = baseScale
        gestureHandler.setInitialPosY(yOffset)

        val node = Node().apply {
            this.renderable = renderable
            localPosition = Vector3(gestureHandler.currentPosX, gestureHandler.currentPosY, gestureHandler.currentPosZ)
            val effectiveScale = gestureHandler.currentScale * modelBaseScale
            localScale = Vector3(effectiveScale, effectiveScale, effectiveScale)
            val yawRot = Quaternion.axisAngle(Vector3(0f, 1f, 0f), gestureHandler.currentYaw)
            val pitchRot = Quaternion.axisAngle(Vector3(1f, 0f, 0f), gestureHandler.currentPitch)
            localRotation = Quaternion.multiply(pitchRot, yawRot)
        }
        scene.addChild(node)
        model3DNode = node

        scene.camera.localPosition = Vector3(0.0f, 0.0f, 0.0f)

        // Dựng phòng 3D thông qua ArRoomStudioBuilder
        context?.let { ctx ->
            roomStudioNode?.let { scene.removeChild(it) }
            val studioNode = ArRoomStudioBuilder.buildRoomStudio(ctx, scene, floorLevelY, gestureHandler.currentPosZ)
            roomStudioNode = studioNode
            if (isCameraMode) {
                studioNode.isEnabled = false
            }
        }

        applyShowroomClearColor()

        if (currentColor != 0) {
            applyColorToModel(currentColor)
        }
    }

    private fun applyColorToModel(colorInt: Int) {
        if (colorInt == 0) return
        val r = android.graphics.Color.red(colorInt) / 255.0f
        val g = android.graphics.Color.green(colorInt) / 255.0f
        val b = android.graphics.Color.blue(colorInt) / 255.0f
        val color = Color(r, g, b, 1.0f)

        try {
            model?.material?.setFloat4("baseColorFactor", color)
        } catch (_: Exception) {}

        val renderableInstance = model3DNode?.renderableInstance
        if (renderableInstance != null) {
            for (i in 0 until 10) {
                try {
                    val mat = renderableInstance.getMaterial(i) ?: break
                    mat.setFloat4("baseColorFactor", color)
                } catch (_: Exception) {}
            }
        }
    }

    private fun applyModelTransform(yaw: Float, pitch: Float, scale: Float, posX: Float, posY: Float, posZ: Float) {
        val node = model3DNode ?: return
        val yawRot = Quaternion.axisAngle(Vector3(0f, 1f, 0f), yaw)
        val pitchRot = Quaternion.axisAngle(Vector3(1f, 0f, 0f), pitch)
        node.localRotation = Quaternion.multiply(pitchRot, yawRot)
        val effectiveScale = scale * modelBaseScale
        node.localScale = Vector3(effectiveScale, effectiveScale, effectiveScale)
        node.localPosition = Vector3(posX, posY, posZ)
    }

    override fun onResume() {
        super.onResume()
        try {
            sceneView3D?.resume()
        } catch (_: Exception) {}
        if (isCameraMode && !cameraManager.isRunning) {
            startCamera()
        }
        applyShowroomClearColor()
    }

    override fun onPause() {
        super.onPause()
        try {
            sceneView3D?.pause()
        } catch (_: Exception) {}
        cameraManager.stopCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            sceneView3D?.destroy()
        } catch (_: Exception) {}
        sceneView3D = null
        model3DNode = null
        roomStudioNode = null
        model = null
        cameraManager.stopCamera()
    }
}

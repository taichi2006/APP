package com.example.feature.product.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.Collections;

public class ARSceneDelegate implements View.OnTouchListener {

    private final Context context;
    private final SceneView sceneView3D;
    private final SurfaceView cameraSurfaceView;
    private final Button btnSwitch;
    private final TextView tvInstruction;

    private Node model3DNode;
    private Renderable model;
    private Node roomStudioNode;
    private float modelBaseScale = 1.0f;
    private boolean isCameraMode = false;

    private float currentYaw = 0f, currentPitch = 0f, currentScale = 1.0f;
    private float currentPosX = 0f, currentPosY = -0.4f, currentPosZ = -1.2f;
    private float previousX, previousY;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private final ScaleGestureDetector scaleDetector;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private HandlerThread cameraThread;
    private Handler cameraHandler;

    public ARSceneDelegate(Context context, SceneView sceneView, SurfaceView cameraSurface, Button btnSwitch, TextView tvInstruction) {
        this.context = context;
        this.sceneView3D = sceneView;
        this.cameraSurfaceView = cameraSurface;
        this.btnSwitch = btnSwitch;
        this.tvInstruction = tvInstruction;

        this.scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                currentScale = Math.max(0.3f, Math.min(3.5f, currentScale * detector.getScaleFactor()));
                applyTransform();
                return true;
            }
        });

        if (sceneView3D != null) sceneView3D.setOnTouchListener(this);
        setupSurfaceHolder();
    }

    public boolean isCameraMode() { return isCameraMode; }

    public void loadModel(String modelUri, int currentColor) {
        String path = (modelUri == null || modelUri.trim().isEmpty() || "arm_chair__furniture.glb".equals(modelUri))
                ? "models/chair_armchair.glb" : (modelUri.startsWith("http") || modelUri.startsWith("models/") ? modelUri : "models/" + modelUri);
        ModelRenderable.builder().setSource(context, Uri.parse(path)).setIsFilamentGltf(true).build()
                .thenAccept(r -> { model = r; displayIn3DScene(currentColor); })
                .exceptionally(t -> {
                    ModelRenderable.builder().setSource(context, Uri.parse("models/chair_armchair.glb")).setIsFilamentGltf(true).build()
                            .thenAccept(fallback -> { model = fallback; displayIn3DScene(currentColor); });
                    return null;
                });
    }

    private void displayIn3DScene(int currentColor) {
        if (model == null || sceneView3D == null) return;
        Scene scene = sceneView3D.getScene();
        if (scene == null) return;
        if (model3DNode != null) scene.removeChild(model3DNode);

        float baseScale = 1.0f, yOffset = -0.4f, floorLevelY = -0.4f;
        if (model.getCollisionShape() instanceof Box) {
            Box box = (Box) model.getCollisionShape();
            float maxDim = Math.max(box.getSize().x, Math.max(box.getSize().y, box.getSize().z));
            if (maxDim > 0.05f) {
                baseScale = 0.85f / maxDim;
                yOffset = -0.3f - (box.getCenter().y * baseScale);
                floorLevelY = yOffset + (box.getCenter().y - box.getSize().y * 0.5f) * baseScale;
            }
        }
        modelBaseScale = baseScale;
        currentPosY = yOffset;

        Node node = new Node();
        node.setRenderable(model);
        node.setLocalPosition(new Vector3(currentPosX, currentPosY, currentPosZ));
        float eff = currentScale * modelBaseScale;
        node.setLocalScale(new Vector3(eff, eff, eff));
        node.setLocalRotation(Quaternion.multiply(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), currentPitch), Quaternion.axisAngle(new Vector3(0f, 1f, 0f), currentYaw)));

        scene.addChild(node);
        model3DNode = node;
        scene.getCamera().setLocalPosition(new Vector3(0f, 0f, 0f));

        if (roomStudioNode != null) scene.removeChild(roomStudioNode);
        roomStudioNode = new Node();
        roomStudioNode.setLocalPosition(new Vector3(0f, floorLevelY, currentPosZ));
        scene.addChild(roomStudioNode);

        MaterialFactory.makeOpaqueWithColor(context, new Color(0.14f, 0.16f, 0.20f))
                .thenAccept(material -> {
                    if (roomStudioNode != null) {
                        roomStudioNode.setRenderable(ShapeFactory.makeCylinder(0.8f, 0.02f, new Vector3(0f, 0f, 0f), material));
                        if (isCameraMode) roomStudioNode.setEnabled(false);
                    }
                });

        applyShowroomClearColor();
        if (currentColor != 0) applyColorToModel(currentColor);
    }

    public void applyColorToModel(int argb) {
        if (model == null) return;
        Color scColor = new Color(android.graphics.Color.red(argb) / 255f, android.graphics.Color.green(argb) / 255f, android.graphics.Color.blue(argb) / 255f, 1.0f);
        for (int i = 0; i < model.getSubmeshCount(); i++) {
            Material mat = model.getMaterial(i);
            if (mat != null) {
                mat.setFloat3("baseColorFactor", scColor.r, scColor.g, scColor.b);
                mat.setFloat4("baseColorFactor", scColor.r, scColor.g, scColor.b, 1.0f);
            }
        }
    }

    public void resetView() {
        currentYaw = 0f;
        currentPitch = 0f;
        currentScale = 1.0f;
        currentPosX = 0f;
        currentPosZ = -1.2f;
        currentPosY = (model != null && model.getCollisionShape() instanceof Box)
                ? -0.3f - (((Box) model.getCollisionShape()).getCenter().y * modelBaseScale) : -0.4f;
        applyTransform();
    }

    private void applyTransform() {
        if (model3DNode == null) return;
        model3DNode.setLocalRotation(Quaternion.multiply(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), currentPitch), Quaternion.axisAngle(new Vector3(0f, 1f, 0f), currentYaw)));
        float eff = currentScale * modelBaseScale;
        model3DNode.setLocalScale(new Vector3(eff, eff, eff));
        model3DNode.setLocalPosition(new Vector3(currentPosX, currentPosY, currentPosZ));
    }

    public void enableCameraMode(boolean enable) {
        isCameraMode = enable;
        if (enable) {
            if (cameraSurfaceView != null) cameraSurfaceView.setVisibility(View.VISIBLE);
            makeSceneViewTransparent(true);
            if (roomStudioNode != null) roomStudioNode.setEnabled(false);
            startCamera();
            if (btnSwitch != null) btnSwitch.setText("Tắt Camera (Xem 3D)");
            if (tvInstruction != null) tvInstruction.setText("Camera AR: 1 ngón xoay • 2 ngón thu phóng");
        } else {
            stopCamera();
            if (cameraSurfaceView != null) cameraSurfaceView.setVisibility(View.GONE);
            makeSceneViewTransparent(false);
            if (roomStudioNode != null) roomStudioNode.setEnabled(true);
            if (btnSwitch != null) btnSwitch.setText("Xem qua Camera");
            if (tvInstruction != null) tvInstruction.setText("1 ngón xoay 360° • 2 ngón thu phóng");
        }
    }

    private void makeSceneViewTransparent(boolean transparent) {
        if (sceneView3D == null) return;
        try {
            if (transparent) {
                sceneView3D.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                sceneView3D.setZOrderMediaOverlay(true);
                if (sceneView3D.getRenderer() != null && sceneView3D.getRenderer().getFilamentView() != null) {
                    sceneView3D.getRenderer().getFilamentView().setBlendMode(com.google.android.filament.View.BlendMode.TRANSLUCENT);
                }
            } else {
                sceneView3D.setZOrderMediaOverlay(false);
                applyShowroomClearColor();
            }
        } catch (Exception ignored) {}
    }

    public void applyShowroomClearColor() {
        if (sceneView3D != null && sceneView3D.getRenderer() != null && sceneView3D.getRenderer().getFilamentRenderer() != null) {
            try {
                com.google.android.filament.Renderer.ClearOptions opt = new com.google.android.filament.Renderer.ClearOptions();
                opt.clear = true;
                opt.clearColor = new float[]{0.10f, 0.12f, 0.16f, 1.0f};
                sceneView3D.getRenderer().getFilamentRenderer().setClearOptions(opt);
            } catch (Exception ignored) {}
        }
    }

    private void setupSurfaceHolder() {
        if (cameraSurfaceView != null) {
            cameraSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override public void surfaceCreated(@NonNull SurfaceHolder h) { if (isCameraMode && cameraDevice == null) startCamera(); }
                @Override public void surfaceChanged(@NonNull SurfaceHolder h, int f, int w, int ht) {}
                @Override public void surfaceDestroyed(@NonNull SurfaceHolder h) { stopCamera(); }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
        if (cameraSurfaceView == null) return;
        Surface surface = cameraSurfaceView.getHolder().getSurface();
        if (surface == null || !surface.isValid() || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return;

        try {
            stopCamera();
            cameraThread = new HandlerThread("CameraBackground");
            cameraThread.start();
            cameraHandler = new Handler(cameraThread.getLooper());

            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (manager == null) return;
            String backCameraId = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = id;
                    break;
                }
            }
            if (backCameraId == null && manager.getCameraIdList().length > 0) backCameraId = manager.getCameraIdList()[0];
            if (backCameraId == null) return;

            manager.openCamera(backCameraId, new CameraDevice.StateCallback() {
                @Override public void onOpened(@NonNull CameraDevice camera) { cameraDevice = camera; createCameraPreview(); }
                @Override public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); cameraDevice = null; }
                @Override public void onError(@NonNull CameraDevice camera, int error) { camera.close(); cameraDevice = null; }
            }, cameraHandler);
        } catch (Exception e) {
            Toast.makeText(context, "Lỗi camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createCameraPreview() {
        if (cameraDevice == null || cameraSurfaceView == null) return;
        try {
            Surface surface = cameraSurfaceView.getHolder().getSurface();
            if (surface == null || !surface.isValid()) return;
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    captureSession = session;
                    try {
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureSession.setRepeatingRequest(builder.build(), null, cameraHandler);
                    } catch (Exception ignored) {}
                }
                @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, cameraHandler);
        } catch (Exception ignored) {}
    }

    private void stopCamera() {
        try {
            if (captureSession != null) { captureSession.close(); captureSession = null; }
            if (cameraDevice != null) { cameraDevice.close(); cameraDevice = null; }
            if (cameraThread != null) { cameraThread.quitSafely(); cameraThread.join(500); cameraThread = null; cameraHandler = null; }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && event.getPointerCount() == 1) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        float x = event.getX(pointerIndex), y = event.getY(pointerIndex);
                        currentYaw += (x - previousX) * 0.45f;
                        currentPitch = Math.max(-45f, Math.min(45f, currentPitch + (y - previousY) * 0.25f));
                        applyTransform();
                        previousX = x;
                        previousY = y;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
        }
        return true;
    }

    public void onResume() {
        try { if (sceneView3D != null) sceneView3D.resume(); } catch (Exception ignored) {}
        if (isCameraMode && cameraDevice == null) startCamera();
        applyShowroomClearColor();
    }

    public void onPause() {
        try { if (sceneView3D != null) sceneView3D.pause(); } catch (Exception ignored) {}
        stopCamera();
    }

    public void onDestroy() {
        try { if (sceneView3D != null) sceneView3D.destroy(); } catch (Exception ignored) {}
        stopCamera();
    }
}

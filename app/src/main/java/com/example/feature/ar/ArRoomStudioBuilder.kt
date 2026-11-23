package com.example.feature.ar

import android.content.Context
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory

/**
 * Xây dựng phòng 3D trực quan gồm Sàn gỗ sồi, Thảm trải sàn, Tường phòng khách và Len chân tường
 * Giúp người dùng dễ dàng hình dung kích thước và tương quan nội thất trong không gian thực tế.
 */
object ArRoomStudioBuilder {

    fun buildRoomStudio(
        context: Context,
        scene: Scene,
        floorY: Float,
        currentPosZ: Float
    ): Node {
        val root = Node()

        // 1. Sàn gỗ sồi tự nhiên hiện đại (Warm Oak Wood Floor)
        MaterialFactory.makeOpaqueWithColor(context, Color(0.85f, 0.77f, 0.67f, 1.0f))
            .thenAccept { floorMat ->
                floorMat.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.45f)
                val floorRenderable = ShapeFactory.makeCube(
                    Vector3(6.0f, 0.04f, 6.0f),
                    Vector3(0f, floorY - 0.02f, currentPosZ),
                    floorMat
                )
                val floorNode = Node().apply {
                    renderable = floorRenderable
                }
                root.addChild(floorNode)
            }

        // 2. Thảm dệt phòng khách êm ái (Warm Cream Rug)
        MaterialFactory.makeOpaqueWithColor(context, Color(0.95f, 0.94f, 0.91f, 1.0f))
            .thenAccept { rugMat ->
                rugMat.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.9f)
                val rugRenderable = ShapeFactory.makeCube(
                    Vector3(2.5f, 0.01f, 2.0f),
                    Vector3(0f, floorY + 0.005f, currentPosZ),
                    rugMat
                )
                val rugNode = Node().apply {
                    renderable = rugRenderable
                }
                root.addChild(rugNode)
            }

        // 3. Tường phòng khách phong cách Scandinavian sáng sủa (Soft Off-White Wall)
        MaterialFactory.makeOpaqueWithColor(context, Color(0.93f, 0.94f, 0.96f, 1.0f))
            .thenAccept { wallMat ->
                wallMat.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.85f)
                val wallRenderable = ShapeFactory.makeCube(
                    Vector3(6.0f, 3.5f, 0.04f),
                    Vector3(0f, floorY + 1.75f, currentPosZ - 2.0f),
                    wallMat
                )
                val wallNode = Node().apply {
                    renderable = wallRenderable
                }
                root.addChild(wallNode)
            }

        // 4. Len chân tường trắng tinh tế (White Skirting Board)
        MaterialFactory.makeOpaqueWithColor(context, Color(0.99f, 0.99f, 0.99f, 1.0f))
            .thenAccept { baseboardMat ->
                val baseboardRenderable = ShapeFactory.makeCube(
                    Vector3(6.0f, 0.12f, 0.05f),
                    Vector3(0f, floorY + 0.06f, currentPosZ - 1.97f),
                    baseboardMat
                )
                val baseboardNode = Node().apply {
                    renderable = baseboardRenderable
                }
                root.addChild(baseboardNode)
            }

        scene.addChild(root)
        return root
    }
}

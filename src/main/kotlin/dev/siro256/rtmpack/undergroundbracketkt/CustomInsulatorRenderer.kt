package dev.siro256.rtmpack.undergroundbracketkt

import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.bindVBO
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.render
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setLightMapCoords
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setMaterial
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setModelView
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setTexture
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.useModel
import jp.ngt.rtm.electric.RenderElectricalWiring
import jp.ngt.rtm.electric.TileEntityConnectorBase
import jp.ngt.rtm.electric.TileEntityElectricalWiring
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.fml.common.Loader
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.properties.Delegates

object CustomInsulatorRenderer : TileEntitySpecialRenderer<TileEntityCustomInsulator>() {
    private var previousFrameIndex = Int.MIN_VALUE

    private var projectionMatrix by Delegates.notNull<Matrix4f>()
    private val matrixBuffer = GLAllocation.createDirectFloatBuffer(16)

    override fun render(
        tileEntity: TileEntityCustomInsulator,
        x: Double, y: Double, z: Double,
        tickProgression: Float,
        destroyStage: Int,
        alpha: Float
    ) {
        val pass = MinecraftForgeClient.getRenderPass()

        RenderElectricalWiring::class.java
            .getDeclaredMethod(
                "renderAllWire",
                if (Loader.isModLoaded("fix-rtm")) {
                    TileEntityElectricalWiring::class.java
                } else {
                    TileEntityConnectorBase::class.java
                },
                Double::class.java,
                Double::class.java,
                Double::class.java,
                Float::class.java,
                Int::class.java
            )
            .apply { isAccessible = true }
            .invoke(RenderElectricalWiring.INSTANCE, tileEntity, x, y, z, tickProgression, pass)

        if (pass != 0) return

        val frameIndex = Minecraft.getMinecraft().frameTimer.index
        if (previousFrameIndex != frameIndex) {
            previousFrameIndex = frameIndex
            projectionMatrix =
                matrixBuffer.apply {
                    rewind()
                    GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, this)
                    rewind()
                }.let { Matrix4f(it) }
        }

        val model = UndergroundBracketKt.customModels["models/undergroundbracketkt/bracket.obj"] as BracketModel

        val resourceSet = tileEntity.resourceState.resourceSet
        val viewMatrix =
            matrixBuffer.apply {
                rewind()
                GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, this)
                rewind()
            }.let { Matrix4f(it) }

        val offset = tileEntity.offset

        val modelMatrix =
            Matrix4f()
                .translate(x.toFloat() + 0.5F, y.toFloat() + 0.5F, z.toFloat() + 0.5F)
                .translate(Vector3f(resourceSet.config.offset))
                .translate(Vector3f(offset.x.toFloat(), offset.y.toFloat(), offset.z.toFloat()))
                .rotateY(-tileEntity.yaw.toRadians())
                .rotateY(tileEntity.offsetYaw.toRadians())
                .rotateZ(180.0F.toRadians())

        val lightMapCoords =
            Vector2f((OpenGlHelper.lastBrightnessX + 8.0F) / 256.0F, (OpenGlHelper.lastBrightnessY + 8.0F) / 256.0F)

        val textureManager = Minecraft.getMinecraft().textureManager
        val texture =
            resourceSet.modelObj.textures.first().material.texture.let {
                @Suppress("UNNECESSARY_SAFE_CALL")
                textureManager.getTexture(it)?.glTextureId ?: run {
                    textureManager.loadTexture(it, SimpleTexture(it))
                    textureManager.getTexture(it).glTextureId
                }
            }
        val shader =
            TexturedShader
                .updateProjection(projectionMatrix)
                .setMaterial(0)
                .setTexture(texture)
                .bindVBO(model.vbo)
                .setLightMapCoords(lightMapCoords)

        shader
            .setModelView(modelMatrix, viewMatrix)
            .useModel(model.staticObjects)
            .render()

        if (tileEntity.resourceState.resourceName.contains("outer")) {
            modelMatrix.rotateY(180.0F.toRadians())
        }

        shader
            .setModelView(modelMatrix, viewMatrix)
            .useModel(model.connectionPoint)
            .render()
    }

    override fun isGlobalRenderer(tileEntity: TileEntityCustomInsulator) = true

    private fun Float.toRadians() = Math.toRadians(toDouble()).toFloat()
}

package dev.siro256.rtmpack.undergroundbracketkt

import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.bindVBO
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.render
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setLightMapCoords
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setMaterial
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setModelView
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.setTexture
import com.github.kotatsu_rtm.kotatsulib.api.shader.TexturedShader.Builder.Companion.useModel
import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.gl.GLStateImpl
import jp.ngt.rtm.electric.RenderElectricalWiring
import jp.ngt.rtm.electric.TileEntityConnectorBase
import jp.ngt.rtm.electric.TileEntityElectricalWiring
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.fml.common.Loader
import org.joml.Matrix4fStack
import org.joml.Vector2f
import org.joml.Vector3f

object CustomInsulatorRenderer : TileEntitySpecialRenderer<TileEntityCustomInsulator>() {
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

        val model = UndergroundBracketKt.customModels["models/undergroundbracketkt/bracket.obj"] as BracketModel

        val resourceSet = tileEntity.resourceState.resourceSet
        val viewMatrix = GLStateImpl.getView()

        val offset = tileEntity.offset

        val modelStack = Matrix4fStack(2)
        modelStack.translate(x.toFloat() + 0.5F, y.toFloat() + 0.5F, z.toFloat() + 0.5F)
        modelStack.translate(Vector3f(resourceSet.config.offset))
        modelStack.translate(Vector3f(offset.x.toFloat(), 0.0F, offset.z.toFloat()))
        modelStack.rotateY(-tileEntity.yaw.toRadians())
        modelStack.rotateY(tileEntity.offsetYaw.toRadians())
        modelStack.rotateZ(180.0F.toRadians())

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
                .updateProjection(GLStateImpl.getProjection())
                .setMaterial(0)
                .setTexture(texture)
                .bindVBO(model.vbo)
                .setLightMapCoords(lightMapCoords)

        shader
            .setModelView(modelStack, viewMatrix)
            .useModel(model.poleBase)
            .render()

        modelStack.pushMatrix()
        modelStack.translate(0.0F, -offset.y.toFloat(), 0.0F)

        shader
            .setModelView(modelStack, viewMatrix)
            .useModel(model.offsetableObjects)
            .render()
            .also {
                if (offset.y.toFloat() == 0.0F) return@also
                it.useModel(model.poleExtensionPipe).render()
            }

        if (tileEntity.resourceState.resourceName.contains("outer"))
            modelStack.rotateY(180.0F.toRadians())

        shader.setModelView(modelStack, viewMatrix).useModel(model.connectionPoint).render()

        modelStack.popMatrix()
    }

    override fun isGlobalRenderer(tileEntity: TileEntityCustomInsulator) = true

    private fun Float.toRadians() = Math.toRadians(toDouble()).toFloat()
}

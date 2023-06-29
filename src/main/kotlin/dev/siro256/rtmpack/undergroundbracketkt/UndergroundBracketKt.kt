package dev.siro256.rtmpack.undergroundbracketkt

import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.model.Model
import com.google.gson.GsonBuilder
import dev.siro256.rtmpack.undergroundbracketkt.UndergroundBracketKt.Companion.MOD_ID
import dev.siro256.rtmpack.undergroundbracketkt.UndergroundBracketKt.Companion.MOD_NAME
import dev.siro256.rtmpack.undergroundbracketkt.UndergroundBracketKt.Companion.MOD_VERSION
import jp.ngt.ngtlib.renderer.model.ModelLoader
import jp.ngt.ngtlib.renderer.model.VecAccuracy
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.cfg.ModelConfig
import jp.ngt.rtm.modelpack.cfg.ResourceConfig
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.ProgressManager
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.URI
import java.util.zip.ZipFile

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION)
@EventBusSubscriber(modid = MOD_ID)
class UndergroundBracketKt {
    @EventHandler
    fun fmlInitEvent(event: FMLInitializationEvent) {
        registerModels()

        if (event.side.isClient) {
            loadCustomShaderModel()
        }
    }

    private fun registerModels() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val path =
            javaClass.protectionDomain.codeSource.location.path
                .removePrefix("jar:").split("!")
                .dropLast(1).joinToString("")

        ZipFile(File(URI(path))).use { file ->
            val modelDefinitions =
                file.entries().toList()
                    .filter { it.name.startsWith("assets/$MOD_ID/model_jsons/", true) && !it.isDirectory }

            val progress = ProgressManager.push("Preparing models", modelDefinitions.size)

            modelDefinitions.forEach { entry ->
                progress.step(entry.name.split("/").last())

                val type = ModelPackManager.INSTANCE.getType(entry.name.split("/")[3])
                val json = file.getInputStream(entry).use { it.readBytes().decodeToString() }
                val resourceConfig = gson.fromJson<ResourceConfig>(json, type.cfgClass)

                if (resourceConfig is ModelConfig) {
                    resourceConfig::class.java
                        .getDeclaredField("name")
                        .apply { isAccessible = true }
                        .set(resourceConfig, "!${MOD_ID}_${resourceConfig.name}")
                }

                ModelPackManager.INSTANCE.registerResourceSet(type, resourceConfig, json)
            }

            ProgressManager.pop(progress)
        }
    }

    private fun loadCustomShaderModel() {
        val progress = ProgressManager.push("Loading custom shading models", 1)
        val model = ResourceLocation("models/undergroundbracketkt/bracket.obj")

        progress.step(model.toString())
        customModels[model.resourcePath] = BracketModel(ModelLoader.loadModel(model, VecAccuracy.MEDIUM).groupObjects)
        ProgressManager.pop(progress)
    }

    companion object {
        const val MOD_ID = "@modId@"
        const val MOD_NAME = "@modName@"
        const val MOD_VERSION = "@modVersion@"

        val logger: Logger = LogManager.getLogger(MOD_NAME)
        val customModels = mutableMapOf<String, Model>()
    }
}

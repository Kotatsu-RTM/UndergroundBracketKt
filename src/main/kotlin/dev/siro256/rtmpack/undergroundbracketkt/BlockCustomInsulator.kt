package dev.siro256.rtmpack.undergroundbracketkt

import jp.ngt.rtm.electric.BlockInsulator
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

object BlockCustomInsulator : BlockInsulator() {
    init {
        registryName = ResourceLocation(UndergroundBracketKt.MOD_ID, "custom_insulator")
    }

    override fun createNewTileEntity(world: World, meta: Int) = TileEntityCustomInsulator().apply { this.world = world }
}

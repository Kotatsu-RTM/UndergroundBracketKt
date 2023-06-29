package dev.siro256.rtmpack.undergroundbracketkt

import jp.ngt.ngtlib.math.Vec3
import jp.ngt.rtm.electric.TileEntityConnectorBase
import jp.ngt.rtm.electric.TileEntityInsulator
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity

class TileEntityCustomInsulator : TileEntityInsulator() {
    var yaw = 0.0F
        set(value) {
            field = value.coerceIn(-360.0F, 360.0F)
        }

    var offset = Vec3(0.0, 0.0, 0.0)
        private set
    var offsetYaw = 0.0F
        private set

    override fun getUpdateTag() = writeToNBT(NBTTagCompound())

    override fun handleUpdateTag(nbt: NBTTagCompound) {
        readFromNBT(nbt)
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity = SPacketUpdateTileEntity(pos, 0, updateTag)

    override fun onDataPacket(net: NetworkManager, packet: SPacketUpdateTileEntity) {
        readFromNBT(packet.nbtCompound)
    }

    override fun readFromNBT(nbt: NBTTagCompound) {
        if (nbt.hasKey("modelYaw")) yaw = nbt.getFloat("modelYaw")

        if (nbt.hasKey("offsetX") && nbt.hasKey("offsetY") && nbt.hasKey("offsetZ") && nbt.hasKey("Yaw")) {
            offset =
                Vec3(
                    nbt.getFloat("offsetX").toDouble(),
                    nbt.getFloat("offsetY").toDouble(),
                    nbt.getFloat("offsetZ").toDouble()
                )
            offsetYaw = nbt.getFloat("Yaw")
        }

        super.readFromNBT(nbt)
    }

    override fun writeToNBT(nbt: NBTTagCompound): NBTTagCompound {
        nbt.apply {
            setFloat("modelYaw", yaw)
        }

        return super.writeToNBT(nbt)
    }

    override fun updateWirePos() {
        val configPos = resourceState.resourceSet.config.wirePos

        val pos =
            Vec3(configPos[0].toDouble(), configPos[1].toDouble(), configPos[2].toDouble())
                .rotateAroundY(yaw)
                .rotateAroundY(90.0F)
                .rotateAroundZ(180.0F)
                .rotateAroundY(offsetYaw)
                .add(offset)

        TileEntityConnectorBase::class.java
            .getDeclaredField("wirePos")
            .apply { isAccessible = true }
            .set(this, pos)
    }
}

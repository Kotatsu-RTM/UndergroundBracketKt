package dev.siro256.rtmpack.undergroundbracketkt

import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.model.Model
import jp.ngt.ngtlib.renderer.model.GroupObject

class BracketModel(groupObjects: List<GroupObject>) : Model(groupObjects) {
    val poleBase = generateDrawGroup("pole_base")
    val offsetableObjects = generateDrawGroup("pipe", "pole_pipe")
    val connectionPoint = generateDrawGroup("connection_point")
    val poleExtensionPipe = generateDrawGroup("pole_pipe_extension")
}

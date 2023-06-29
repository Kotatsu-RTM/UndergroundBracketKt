package dev.siro256.rtmpack.undergroundbracketkt

import com.github.kotatsu_rtm.kotatsulib.mc1_12_2.api.model.Model
import jp.ngt.ngtlib.renderer.model.GroupObject

class BracketModel(groupObjects: List<GroupObject>) : Model(groupObjects) {
    val staticObjects = generateDrawGroup("pipe", "pole")
    val connectionPoint = generateDrawGroup("connection_point")
}

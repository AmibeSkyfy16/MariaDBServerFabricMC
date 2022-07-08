package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.mariadbserverfabricmc.json.Defaultable
import ch.skyfy.mariadbserverfabricmc.json.JsonData

object Configs {

    val MOD_CONFIG: JsonData<ModConfig> = JsonData.invoke<ModConfig, ModConfigDefault>("config.json")

    class ModConfigDefault : Defaultable<ModConfig>{
        override fun getDefault(): ModConfig = ModConfig(3308)
    }

}
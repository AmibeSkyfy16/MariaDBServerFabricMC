package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.mariadbserverfabricmc.EmbeddedDatabase
import ch.skyfy.mariadbserverfabricmc.json.Defaultable
import ch.skyfy.mariadbserverfabricmc.json.JsonData
import kotlin.io.path.absolutePathString

object Configs {

    val MOD_CONFIG: JsonData<ModConfig> = JsonData.invoke("config.json")

    class ModConfigDefault : Defaultable<ModConfig>{
        override fun getDefault(): ModConfig = ModConfig(3307, EmbeddedDatabase.databaseFolder.resolve("dataDir").absolutePathString(), null)
    }

}
package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.mariadbserverfabricmc.EmbeddedDatabase
import ch.skyfy.mariadbserverfabricmc.json.Defaultable
import ch.skyfy.mariadbserverfabricmc.json.JsonData
import kotlin.io.path.absolutePathString

object Configs {

    val MOD_CONFIG: JsonData<ModConfig, ModConfigDefault> = JsonData("config.json", ModConfig::class,ModConfig::class, ModConfigDefault::class)

    public class ModConfigDefault : Defaultable<ModConfig>{
        public override fun getDefault(): ModConfig {
            return ModConfig(3307, EmbeddedDatabase.databaseFolder.resolve("dataDir").absolutePathString(), null)
        }

    }

}
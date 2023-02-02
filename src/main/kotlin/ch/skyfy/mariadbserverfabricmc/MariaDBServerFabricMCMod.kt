package ch.skyfy.mariadbserverfabricmc


import ch.skyfy.mariadbserverfabricmc.utils.setupConfigDirectory
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate")
class MariaDBServerFabricMCMod : DedicatedServerModInitializer {

    companion object {
        const val MOD_ID: String = "mariadb_server_fabricmc"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)

        //        val MARIADB_DIRECTORY: Path = CONFIG_DIRECTORY.resolve("mariadb-10.10.2")
//        val DATA_DIRECTORY: Path = MARIADB_DIRECTORY.resolve("data")
        val LOGGER: Logger = LogManager.getLogger(MariaDBServerFabricMCMod::class.java)
    }

    init {
        setupConfigDirectory()
//        setupMariaDBDirectory()
//        setupDataDirectory()

        EmbeddedDatabase()
    }

    override fun onInitializeServer() {}


}
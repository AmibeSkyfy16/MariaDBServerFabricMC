package ch.skyfy.mariadbserverfabricmc


import ch.skyfy.mariadbserverfabricmc.config.Configs
import ch.skyfy.mariadbserverfabricmc.exceptions.MariaDBServerFabricMCModException
import ch.skyfy.mariadbserverfabricmc.utils.ReflectionUtils
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

        val LOGGER: Logger = LogManager.getLogger(MariaDBServerFabricMCMod::class.java)
    }


    init {
        createConfigDir()
        ReflectionUtils.loadClassesByReflection(arrayOf(Configs::class.java))
//        EmbeddedDatabase().startMariaDBServer()
    }

    override fun onInitializeServer() {

    }

    private fun createConfigDir() {
        try {
            val file = CONFIG_DIRECTORY.toFile()
            if (!file.exists()) file.mkdir()
        } catch (e: java.lang.Exception) {
            LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
            throw MariaDBServerFabricMCModException(e)
        }
    }

}
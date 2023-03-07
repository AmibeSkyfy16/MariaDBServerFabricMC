package ch.skyfy.mariadbserverfabricmc.prelaunch


import ch.skyfy.json5configlib.ConfigManager
import ch.skyfy.mariadbserverfabricmc.MariaInstaller
import ch.skyfy.mariadbserverfabricmc.config.Configs
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate")
class MariaDBServerFabricMCModPreLauncher : PreLaunchEntrypoint {

    companion object {
        const val MOD_ID: String = "mariadb_server_fabricmc"
        val CONFIG_DIRECTORY: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        val LOGGER: Logger = LogManager.getLogger(MariaDBServerFabricMCModPreLauncher::class.java)
    }

    init {
//        setupConfigDirectory()
        ConfigManager.loadConfigs(arrayOf(Configs::class.java))

        val m = MariaInstaller()

        Runtime.getRuntime().addShutdownHook(Thread {
            println("end ________")
            m.stopMaria()
        })

    }

    override fun onPreLaunch() {

    }


}
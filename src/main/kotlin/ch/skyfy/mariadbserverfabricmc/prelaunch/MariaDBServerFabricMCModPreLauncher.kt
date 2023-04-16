package ch.skyfy.mariadbserverfabricmc.prelaunch


import ch.skyfy.json5configlib.ConfigManager
import ch.skyfy.mariadbserverfabricmc.config.Configs
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
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

    var requiredShutdownHook = true

    init {
        println(FabricLoader.getInstance().environmentType.name)

        ConfigManager.loadConfigs(arrayOf(Configs::class.java))

        val mariaInstaller = MariaInstaller()

        ServerLifecycleEvents.SERVER_STOPPING.register {
            requiredShutdownHook = false
            mariaInstaller.stopMaria()
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            if(requiredShutdownHook) mariaInstaller.stopMaria(true)
        })
    }

    override fun onPreLaunch() {}

}
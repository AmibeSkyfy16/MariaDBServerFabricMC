package ch.skyfy.mariadbserverfabricmc

import ch.skyfy.mariadbserverfabricmc.config.Configs
import ch.skyfy.mariadbserverfabricmc.mariadbkotlin.DB
import ch.skyfy.mariadbserverfabricmc.mariadbkotlin.DBConfig
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.nio.file.Path

class EmbeddedDatabase {

    companion object {
        private val databaseFolder: Path = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("database")
    }

    private val db: DB

    init {
        val dbconfig = DBConfig.Builder(
            port = Configs.MOD_CONFIG.data.port,
            mariaDBVersion = DBConfig.MariaDBVersion.STABLE_10_8_3,
            installationDir = databaseFolder
        ).build()

        db = DB(dbconfig)
        db.setupFiles()
        registerEvents()
        db.start()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register { db.stop() }
    }

}
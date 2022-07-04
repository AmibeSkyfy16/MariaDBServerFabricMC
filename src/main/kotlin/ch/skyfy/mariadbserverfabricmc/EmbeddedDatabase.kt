package ch.skyfy.mariadbserverfabricmc

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod.Companion.LOGGER
import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod.Companion.MOD_ID
import ch.skyfy.mariadbserverfabricmc.config.Configs
import ch.vorburger.exec.ManagedProcessException
import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class EmbeddedDatabase {

    companion object {
        private const val databaseFolderName: String = "database"
        val databaseFolder: Path = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve(databaseFolderName)
        private const val mariadbFolderName: String = "mariadb-10.8.3-winx64"
        val mariadbFolder: Path = databaseFolder.resolve(mariadbFolderName)
    }

    val db: DB

    init {

        if (!databaseFolder.exists()) databaseFolder.toFile().mkdir()

        registerEvents()
        installMariaDB()

        val builder = DBConfigurationBuilder.newBuilder()

        val config = Configs.MOD_CONFIG.data
        builder.port = config.port

        if (config.baseDir != null) {
            builder.isUnpackingFromClasspath = false
            builder.baseDir = config.baseDir
        }
        if(config.dataDir != null)
            builder.dataDir = config.dataDir

        db = DB.newEmbeddedDB(builder.build())
    }

    /**
     * If this is the first time the minecraft server is started with the mod. Then we have to install (copy and extract) the files for the mariadb server
     */
    private fun installMariaDB() {
        val dest: Path = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("$mariadbFolderName.zip")

        val t3 = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/mariadb_server_fabricmc/$mariadbFolderName.zip")
        if (!dest.exists() && !mariadbFolder.exists()) {
            Files.copy(t3.get(), dest, StandardCopyOption.REPLACE_EXISTING)
            LOGGER.info("Copying files for MariaDB server in ${dest.parent.absolutePathString()}")
        }

        if (dest.exists() && !mariadbFolder.exists()) {
            LOGGER.info("Extracting files for MariaDB server in ${dest.parent.absolutePathString()}")
            LOGGER.info("dest.absolutePathString(): ${dest.absolutePathString()}")
            ZipFile(dest.absolutePathString()).extractAll(mariadbFolder.toAbsolutePath().toString())
            dest.deleteIfExists()
        }
    }

    fun startMariaDBServer() {
        LOGGER.info("Starting MariaDB server \uD83D\uDE80 🚀")
        try {
            db.start()
        } catch (e: ManagedProcessException) {
            e.printStackTrace()
            LOGGER.info("MariaDB Server is already started")
        }
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPED.register {
            db.stop()
        }
    }


}
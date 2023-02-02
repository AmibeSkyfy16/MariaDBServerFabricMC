package ch.skyfy.mariadbserverfabricmc

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod.Companion.MOD_ID
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import org.buildobjects.process.ProcBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

class EmbeddedDatabase {

    companion object {
//        private const val databaseFolderName: String = "database"
//        val databaseFolder: Path = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve(databaseFolderName)
//        private const val mariadbFolderName: String = "mariadb-10.8.3-winx64"
//        val mariadbFolder: Path = databaseFolder.resolve(mariadbFolderName)
    }

//    val db: DB

    init {

//        if (!databaseFolder.exists()) databaseFolder.toFile().mkdir()

        registerEvents()
//        installMariaDB()
        startMariaDBServer()

//        val builder = DBConfigurationBuilder.newBuilder()
//
//        val config = Configs.MOD_CONFIG.data
//        builder.port = config.port
//
//        if (config.baseDir != null) {
//            builder.isUnpackingFromClasspath = false
//            builder.baseDir = config.baseDir
//        }
//        if(config.dataDir != null)
//            builder.dataDir = config.dataDir
//
//        db = DB.newEmbeddedDB(builder.build())
    }

    /**
     * If this is the first time the minecraft server is started with the mod. Then we have to install (copy and extract) the files for the mariadb server
     */
    private fun installMariaDB() {

        val t3 = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/mariadb_server_fabricmc/mariadb-10.10.2-winx64.zip")

        val dest = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2-winx64.zip")
        if (!dest.exists()) {
            Files.copy(t3.get(), MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2-winx64.zip"))
            ZipFile(dest.absolutePathString()).extractAll(MariaDBServerFabricMCMod.CONFIG_DIRECTORY.absolutePathString())
            val newFolder = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2-winx64")
            Files.move(newFolder, newFolder.resolveSibling("mariadb-10.10.2")); // rename
            val mariadbFolder = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2").resolve("data")
            mariadbFolder.createDirectory()
            Files.deleteIfExists(dest)
        }
    }

    fun startMariaDBServer() {

        val workingDir =  MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2").resolve("bin")


//        ProcessBuilder().directory(workingDir.toFile()).command(".\\\\mariadb-install-db.exe --datadir='..\\\\data\\\\' --password='Pa\\\$\\\$w0rd' --port='3307'\"")

        ProcBuilder("mariadb-install-db.exe").withOutputConsumer {
            val reader = BufferedReader(InputStreamReader(it))
            println("line:" + reader.readLine())
            println("line2:" + reader.readLine())
        }.withTimeoutMillis(6000)
            .withWorkingDirectory(Paths.get("E:\\tmp\\coding\\MariaDBServerFabricMC\\run\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin").toFile())
            .run()

        println("here")
        Thread.sleep(5000)
        println("done")
//        LOGGER.info("Starting MariaDB server \uD83D\uDE80 🚀")
//        try {
//            db.start()
//        } catch (e: ManagedProcessException) {
//            e.printStackTrace()
//            LOGGER.info("MariaDB Server is already started")
//        }
    }

    private fun registerEvents() {
//        ServerLifecycleEvents.SERVER_STOPPED.register {
//            db.stop()
//        }
    }


}
package ch.skyfy.mariadbserverfabricmc

import ch.skyfy.json5configlib.update
import ch.skyfy.mariadbserverfabricmc.config.Configs
import ch.skyfy.mariadbserverfabricmc.config.MariaDBInstallationProgress
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.CONFIG_DIRECTORY
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.MOD_ID
import kotlinx.coroutines.*
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.notExists

class MariaInstaller(
    private val mariaZipFileName: String = MARIADB_ZIP_FILE_NAME,
    private val mariaRootFolderName: String = MARIADB_ROOT_FOLDER_NAME,
    private val mariaRootFolderPath: Path = CONFIG_DIRECTORY.resolve(mariaRootFolderName),
    private val mariaBinFolderPath: Path = mariaRootFolderPath.resolve("bin"),
    private val mariaDataFolderPath: Path = mariaRootFolderPath.resolve("data"),
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

    companion object {
        const val MARIADB_ROOT_FOLDER_NAME = "mariadb-10.10.2-winx64"
        const val MARIADB_ZIP_FILE_NAME = "mariadb-10.10.2-winx64.zip"
    }

    init {
        copyMariaZipFromAssetToConfigFolder()
        installMaria()
        startMaria()
    }

    /**
     * If this is the first time the minecraft server is started with the mod. Then we have to install (copy and extract) the files for the mariadb server
     */
    private fun copyMariaZipFromAssetToConfigFolder() {
        MariaDBServerFabricMCModPreLauncher.LOGGER.info("HEAD -> Method -> copyMariaZipFromAssetToConfigFolder")

        if (Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.copyMariadbZipFromAssetToConfigFolderSuccess) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.info("RETURN -> Mariadb folder has already been copied")
            return
        }

        val embeddedMariaZipFile = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/mariadb_server_fabricmc/$mariaZipFileName")

        val copiedMariaZipFile = CONFIG_DIRECTORY.resolve(mariaZipFileName) // mc-server\config\mariadb_server_fabricmc\mariadb-10.10.2-winx64.zip
        if (!copiedMariaZipFile.exists()) {
            Files.copy(embeddedMariaZipFile.get(), copiedMariaZipFile)
            ZipFile(copiedMariaZipFile.absolutePathString()).extractAll(CONFIG_DIRECTORY.absolutePathString())
            if (mariaDataFolderPath.notExists()) mariaDataFolderPath.createDirectory()
            Files.deleteIfExists(copiedMariaZipFile)
            Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::copyMariadbZipFromAssetToConfigFolderSuccess, true)
            MariaDBServerFabricMCModPreLauncher.LOGGER.info("Maria folder has been copied successfully !")
//            val newFolder = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2-winx64")
//            Files.move(newFolder, newFolder.resolveSibling("mariadb-10.10.2")); // rename
//            mariadbBinFolder = MariaDBServerFabricMCMod.CONFIG_DIRECTORY.resolve("mariadb-10.10.2").resolve("data")
        }
    }

    private fun installMaria() {
        MariaDBServerFabricMCModPreLauncher.LOGGER.info("HEAD -> Method -> installMaria")

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.copyMariadbZipFromAssetToConfigFolderSuccess) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.error("\tmethod installMaria() require copyMariadbZipFromAssetToConfigFolderSuccess to true but false was found")
            return
        }

        if (Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.mariaInstallSuccess) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.info("RETURN -> Maria has already been installed with mariadb-install-db.exe")
            return
        }

        val pb = ProcessBuilder(
//            *arrayOf(
//                "cmd.exe",
//                "/c",
////                "mariadb-install-db.exe --datadir=${mariaDataFolderPath.absolutePathString()} --password=12345678"
//                "mariadb-install-db.exe --datadir=\"E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2-winx64\\data\" --password=12345678"
//            )

            *arrayOf(
                mariaBinFolderPath.resolve("mariadb-install-db.exe").normalize().absolutePathString(), // mc-server\config\mariadb_server_fabricmc\mariadb-10.10.2-winx64\bin\mariadb-install-db.exe
//                "--datadir=\"\"${mariaDataFolderPath.normalize().absolutePathString()}\"\"",
                "--datadir=${mariaDataFolderPath.normalize().absolutePathString()}",
                "--password=${Configs.DB_CONFIG.serializableData.password}"
            )
        ).directory(mariaBinFolderPath.toFile()).redirectErrorStream(true)

        val process = pb.start()

        BufferedReader(InputStreamReader(process.inputStream)).use { input ->
            val lastLineMessage = "Creation of the database was successful"
            var line: String?
            while (input.readLine().also { line = it } != null) {
                line?.let { noNullLine ->
                    println(noNullLine)
                    if (noNullLine == lastLineMessage)
                        Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::mariaInstallSuccess, true)
                }
            }
        }
    }

    private fun startMaria() {
        MariaDBServerFabricMCModPreLauncher.LOGGER.info("HEAD -> method -> startMaria()")

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.copyMariadbZipFromAssetToConfigFolderSuccess) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.error("\tmethod startMaria() require copyMariadbZipFromAssetToConfigFolderSuccess to true but false was found")
            return
        }

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.mariaInstallSuccess) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.error("\tmethod startMaria() require mariaInstallSuccess to true but false was found")
            return
        }

        val processBuilder = ProcessBuilder(
            *arrayOf(
                mariaBinFolderPath.resolve("mysqld.exe").normalize().absolutePathString(),
                "--console",
                "--port=${Configs.DB_CONFIG.serializableData.port}",
                "--datadir=${mariaDataFolderPath.normalize().absolutePathString()}"
//                "--datadir=\"\"E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2-winx64\\data\"\""
            )
        ).directory(mariaBinFolderPath.toFile()).redirectErrorStream(true)

        var started = false
        val countDownLatch = CountDownLatch(1)
        // Status check
        launch {
            while (true) {
                delay(500)
                val builder = ProcessBuilder(
                    *arrayOf(
                        mariaBinFolderPath.resolve("mysqladmin.exe").normalize().absolutePathString(),
                        "--user=root",
                        "--password=${Configs.DB_CONFIG.serializableData.password}",
                        "--port=${Configs.DB_CONFIG.serializableData.port}",
                        "status"
                    )
                ).directory(mariaBinFolderPath.toFile()).redirectErrorStream(true)

                val process = withContext(Dispatchers.IO) { builder.start() }

                BufferedReader(InputStreamReader(process.inputStream)).use { input ->
                    val successMessage = arrayOf("Uptime:", "Threads:", "Questions:") // means mysqld.exe is running
                    var l: String?
                    while (input.readLine().also { l = it } != null) {
                        l?.let { noNullLine ->
                            if (successMessage.all { noNullLine.contains(it) }) {
                                started = true
                            }
                        }
                    }
                }
                countDownLatch.countDown()
                if (started) break
            }
        }

        MariaDBServerFabricMCModPreLauncher.LOGGER.info("Checking if mariadb server is already started on the computer. Please wait a few seconds")
        countDownLatch.await()

        if (started) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.info("Maria is already started")
            return
        }

        val process = processBuilder.start()
        var successStarted = false
        val countDownLatch2 = CountDownLatch(1)

        launch {
            BufferedReader(InputStreamReader(process.inputStream)).use { input ->
                val successMessage = "ready for connections"
                val readyKeyWords = arrayOf("Version:", "socket:", "port:") // Last printed line by mysqld.exe
                var line: String?
                while (input.readLine().also { line = it } != null) {
                    line?.let { noNullLine ->
                        MariaDBServerFabricMCModPreLauncher.LOGGER.info(noNullLine)
                        if (noNullLine.contains(successMessage)) successStarted = true
                        if (successStarted && readyKeyWords.all { keyWord -> noNullLine.contains(keyWord) }) {
                            started = true
                            countDownLatch2.countDown()
                        }
                    }
                }
            }
        }

        MariaDBServerFabricMCModPreLauncher.LOGGER.info("MariaDB server is starting ...")
        countDownLatch2.await(10, TimeUnit.SECONDS)

        if (started) {
            MariaDBServerFabricMCModPreLauncher.LOGGER.info("MariaDB server has been started successfully !")
        }
    }

    fun stopMaria() {
        println("Stopping maria")
    }

}
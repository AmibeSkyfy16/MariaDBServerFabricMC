package ch.skyfy.mariadbserverfabricmc.prelaunch

import ch.skyfy.json5configlib.update
import ch.skyfy.mariadbserverfabricmc.config.Configs
import ch.skyfy.mariadbserverfabricmc.config.MariaDBInstallationProgress
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.CONFIG_DIRECTORY
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.LOGGER
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.MOD_ID
import kotlinx.coroutines.*
import net.fabricmc.loader.api.FabricLoader
import net.lingala.zip4j.ZipFile
import org.apache.logging.log4j.Level
import org.fusesource.jansi.Ansi
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
        const val MARIADB_ROOT_FOLDER_NAME = "mariadb-10.11.2-winx64"
        const val MARIADB_ZIP_FILE_NAME = "mariadb-10.11.2-winx64.zip"
    }

    private val stopMariaDB: CountDownLatch = CountDownLatch(1)

    private var mariadbServerProcess: Process? = null

    init {
        testSdtout()

        copyMariaZipFromAssetToConfigFolder()
        installMaria()
        startMaria()
    }

    private fun testSdtout() {
        // ------------- SOME TEST -------------
//        LOGGER.info("Printing emojis \uD83D\uDE0E 😹 \uD83D\uDC69\u200D\uD83D\uDD27 ☂️")
//        LOGGER.info(Ansi.ansi().eraseScreen().fg(Ansi.Color.RED).a("RED RED RED 1☂️ \uD83D\uDE39").reset())
//        LOGGER.info(Ansi.ansi().eraseLine().fg(Ansi.Color.RED).a("RED RED RED 2☂️ \uD83D\uDE39").reset())
//        LOGGER.info(Ansi.ansi().eraseLine(Ansi.Erase.FORWARD).fg(Ansi.Color.RED).a("RED RED RED 3☂️ \uD83D\uDE39").reset())
//        LOGGER.info(Ansi.ansi().eraseLine(Ansi.Erase.BACKWARD).fg(Ansi.Color.RED).a("RED RED RED 4☂️ \uD83D\uDE39").reset())
//        LOGGER.info(Ansi.ansi().eraseLine(Ansi.Erase.ALL).fg(Ansi.Color.RED).a("RED RED RED 5☂️ \uD83D\uDE39").reset())
//        LOGGER.info(Ansi.ansi().fg(Ansi.Color.RED).a("RED RED RED 6☂️ \uD83D\uDE39").reset())
//        println("\r\n")
//        println(Ansi.ansi().eraseScreen().fg(Ansi.Color.RED).a("RED RED RED ☂️").reset())
//        println(Ansi.ansi().eraseScreen().fg(Ansi.Color.MAGENTA).a("MAGENTA MAGENTA MAGENTA ☂️").reset())
//        println(Ansi.ansi().eraseScreen().fg(Ansi.Color.YELLOW).a("YELLOW YELLOW YELLOW ☂️").reset())
// ------------- SOME TEST -------------
    }

    /**
     * If this is the first time the minecraft server is started with the mod. Then we have to install (copy and extract) the files for the mariadb server
     */
    private fun copyMariaZipFromAssetToConfigFolder() {
//        MariaDBServerFabricMCModPreLauncher.LOGGER.info("HEAD -> Method -> copyMariaZipFromAssetToConfigFolder()")

        if (Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.copyMariadbZipFromAssetToConfigFolderSuccess) {
//            LOGGER.log(Level.INFO, "The Mariadb folder has already been copied ✅")
            LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The folder \"${MARIADB_ZIP_FILE_NAME.substringBeforeLast(".")}\" has already been copied ✅").reset())
            return
        }

        val embeddedMariaZipFile = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("assets/mariadb_server_fabricmc/$mariaZipFileName")

        val copiedMariaZipFile = CONFIG_DIRECTORY.resolve(mariaZipFileName) // mc-server\config\mariadb_server_fabricmc\mariadb-10.10.2-winx64.zip
        if (!copiedMariaZipFile.exists()) {
//            LOGGER.log(Level.INFO, "The Mariadb folder does not exist yet and is going to be copied \uD83D\uDD1C \uD83D\uDD1C")
            LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The folder \"${MARIADB_ZIP_FILE_NAME.substringBeforeLast(".")}\" does not exist yet and is going to be copied \uD83D\uDD1C").reset())

            Files.copy(embeddedMariaZipFile.get(), copiedMariaZipFile)
            ZipFile(copiedMariaZipFile.absolutePathString()).extractAll(CONFIG_DIRECTORY.absolutePathString())
            if (mariaDataFolderPath.notExists()) mariaDataFolderPath.createDirectory()
            Files.deleteIfExists(copiedMariaZipFile)
            Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::copyMariadbZipFromAssetToConfigFolderSuccess, true)

//            LOGGER.log(Level.INFO, "The Mariadb folder has been copied successfully \uD83C\uDFAF")
            LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.GREEN).a("The folder \"${MARIADB_ZIP_FILE_NAME.substringBeforeLast(".")}\" has been copied successfully \uD83C\uDFAF").reset())
        }
    }

    private fun installMaria() {

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.copyMariadbZipFromAssetToConfigFolderSuccess) {
            LOGGER.log(Level.ERROR, Ansi.ansi().eraseLine().fg(Ansi.Color.RED).a("The value for boolean field \"copyMariadbZipFromAssetToConfigFolderSuccess\" must be true, but false was found !").reset())
            return
        }

        if (Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.mariaInstallSuccess) {
            LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server has already been installed with command « mariadb-install-db.exe »").reset())
            return
        }

        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server will be installed in a few seconds ...").reset())

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
                    LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.CYAN).a("output from mariadb-install-db.exe: $noNullLine").reset())
//                    println(noNullLine)

                    if (noNullLine == lastLineMessage) {
                        Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::mariaInstallSuccess, true)
                        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.GREEN).a("Maria has been successfully installed !").reset())
                    }
                }
            }
        }
    }

    private fun startMaria() {

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.copyMariadbZipFromAssetToConfigFolderSuccess) {
//            MariaDBServerFabricMCModPreLauncher.LOGGER.error("\tmethod startMaria() require copyMariadbZipFromAssetToConfigFolderSuccess to true but false was found")
            LOGGER.log(Level.ERROR, Ansi.ansi().eraseLine().fg(Ansi.Color.RED).a("The value for the boolean field \"copyMariadbZipFromAssetToConfigFolderSuccess\" must be true, but false was found ❌").reset())
            return
        }

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.mariaInstallSuccess) {
//            MariaDBServerFabricMCModPreLauncher.LOGGER.error("\tmethod startMaria() require mariaInstallSuccess to true but false was found")
            LOGGER.log(Level.ERROR, Ansi.ansi().eraseLine().fg(Ansi.Color.RED).a("The value for the boolean field \"mariaInstallSuccess\" must be true, but false was found ❌").reset())
            return
        }

        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server will be started in a few seconds ...").reset())

        var started = false
        val countDownLatch = CountDownLatch(1)

        // First, we checked with command: mysqladmin.exe status --user=root --password='Pa$$w0rd' --port=3307
        // if the mariadb server is already running
        launch {
//            while (true) {
            delay(500)
            val builder = ProcessBuilder(
                *arrayOf(
                    mariaBinFolderPath.resolve("mysqladmin.exe").normalize().absolutePathString(),
                    "status",
                    "--user=root",
                    "--password=${Configs.DB_CONFIG.serializableData.password}",
                    "--port=${Configs.DB_CONFIG.serializableData.port}",
                )
            ).directory(mariaBinFolderPath.toFile()).redirectErrorStream(true)

            val process = withContext(Dispatchers.IO) { builder.start() }

            BufferedReader(InputStreamReader(process.inputStream)).use { input ->
                val successMessage = arrayOf("Uptime:", "Threads:", "Questions:") // ir means that the process mysqld.exe is running
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

//                if (started) break
//            }
        }

//        MariaDBServerFabricMCModPreLauncher.LOGGER.info("Checking if the MariaDB server is already started on the computer. Please wait a few seconds")
        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("Checking if the MariaDB server is already started on the computer. Please wait a few seconds").reset())
        countDownLatch.await()

        if (started) {
            LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server is already started on the computer \uD83D\uDE80").reset())
            Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::mariaStarted, true) // make sure the value is set to started at this time
//            MariaDBServerFabricMCModPreLauncher.LOGGER.info("Maria is already started")
            return
        }

        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server has not been launched yet, but should be in a few seconds").reset())

        val processBuilder = ProcessBuilder(
            *arrayOf(
                mariaBinFolderPath.resolve("mysqld.exe").normalize().absolutePathString(),
                "--console",
                "--port=${Configs.DB_CONFIG.serializableData.port}",
                "--datadir=${mariaDataFolderPath.normalize().absolutePathString()}"
//                "--datadir=\"\"E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2-winx64\\data\"\""
            )
        ).directory(mariaBinFolderPath.toFile()).redirectErrorStream(true)

        mariadbServerProcess = processBuilder.start()
        var successStarted = false
        val countDownLatch2 = CountDownLatch(1)

        launch {
            BufferedReader(InputStreamReader(mariadbServerProcess!!.inputStream)).use { input ->
                val successMessage = "ready for connections"
                val readyKeyWords = arrayOf("Version:", "socket:", "port:") // Last printed line by mysqld.exe
                val mariadbServerStoppedMessage = "InnoDB: Shutdown completed;"
                var line: String?
                while (input.readLine().also { line = it } != null) {
                    line?.let { noNullLine ->
//                        MariaDBServerFabricMCModPreLauncher.LOGGER.info(noNullLine)
                        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.CYAN).a("output from mysqld.exe: $noNullLine").reset())
//                        println(Ansi.ansi().eraseLine().fg(Ansi.Color.CYAN).a("output from mysqld.exe: $noNullLine").reset())

                        if (noNullLine.contains(successMessage)) successStarted = true
                        if (successStarted && readyKeyWords.all { keyWord -> noNullLine.contains(keyWord) }) {
                            started = true
                            Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::mariaStarted, true) // make sure the value is set to started at this time
                            countDownLatch2.countDown()
                        }

                        if (noNullLine.contains(mariadbServerStoppedMessage, ignoreCase = true)) {
                            Configs.MARIADB_INSTALLATION_PROGRESS.update(MariaDBInstallationProgress::mariaStarted, false) // make sure the value is set to started at this time
//                            stopMariaDB.countDown()
                        }

                    }
                }
            }
        }

//        MariaDBServerFabricMCModPreLauncher.LOGGER.info("MariaDB server is starting ...")
        countDownLatch2.await(20, TimeUnit.SECONDS) // Wait a bit and let the output from mysqld.exe printing the progression in the console

        if (started) {
//            MariaDBServerFabricMCModPreLauncher.LOGGER.info("MariaDB server has been started successfully ! 🚀")
            LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.GREEN).a("The MariaDB server has been started successfully ! \uD83D\uDE80").reset())
        }
    }

    fun stopMaria(fromShutDownHook: Boolean = false) {

        if (!Configs.MARIADB_INSTALLATION_PROGRESS.serializableData.mariaStarted) {
            if (!fromShutDownHook)
                LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server can't be stopped, because it hasn't even been started").reset())
            else {
                LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("Trying to stop the MariaDB server from a shutdown hook (2nd attempt). The MariaDB server can't be stopped, because it hasn't even been started").reset())
                LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server can't be stopped, because it hasn't even been started or it has already been stopped").reset())
            }
            return
        }

        LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server will be shutdown in a few seconds").reset())

        val processBuilder = ProcessBuilder(
            *arrayOf(
                mariaBinFolderPath.resolve("mysqladmin.exe").normalize().absolutePathString(),
                "shutdown",
                "--user=root",
                "--password=${Configs.DB_CONFIG.serializableData.password}",
                "--port=${Configs.DB_CONFIG.serializableData.port}"
            )
        ).directory(mariaBinFolderPath.toFile()).redirectErrorStream(true)

        processBuilder.start() // Will stop the mariadb server and then the process mysqld.exe will be stopped

        // As soon as he is not alive anymore, it means the mariadb server has been stopped
        if (mariadbServerProcess != null) {
            val time = System.currentTimeMillis()
            var timedOut = false
            while (mariadbServerProcess!!.isAlive) {
                Thread.sleep(100)
                if ((System.currentTimeMillis() - time) / 1000 % 60 >= 20) {
                    println("Timeout, process mysqld.exe still alive, but the minecraft server will stop now")
                    timedOut = true
                    break
                }
            }
            if(!timedOut) LOGGER.log(Level.INFO, Ansi.ansi().eraseLine().fg(Ansi.Color.YELLOW).a("The MariaDB server has been stopped").reset())
        }

    }

}
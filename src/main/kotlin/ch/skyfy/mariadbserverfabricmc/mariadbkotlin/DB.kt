@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.mariadbserverfabricmc.mariadbkotlin

import ch.skyfy.mariadbserverfabricmc.utils.UnzipUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.channels.Channels.newChannel
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

@Suppress("unused")
class DB(
    private val dbConfig: DBConfig,
    var setupFilesCallback: Consumer<DB> = Consumer<DB> {},
    var startedCallback: Consumer<DB> = Consumer<DB> {},
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger(DB::class.java)
    }

    var isInstalled = false
    var isStarted = false

    fun setupFiles() {
        if (!dbConfig.isRunInThread) setupFilesImpl()
        else launch {
            setupFilesImpl()
            setupFilesCallback.accept(this@DB)
        }
    }

    private fun setupFilesImpl() {

        if (dbConfig.mariaDBFolder.exists()) {
            LOGGER.info("MariaDB is already installed here: ${dbConfig.mariaDBFolder.absolutePathString()}")
            isInstalled = true
            return
        }

        LOGGER.info("Setting up files ...")

        download()
        extract()
        install()
        isInstalled = true
    }

    private fun download() {

        if (!dbConfig.installationDir.exists()) dbConfig.installationDir.createDirectories()

        try {
            FileOutputStream(dbConfig.downloadedMaria.toFile()).channel.transferFrom(newChannel(URL(DBConfig.VERSION[dbConfig.mariadbVersion]?.get(dbConfig.os)).openStream()), 0, Long.MAX_VALUE)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }

    /**
     * I found a lot of examples to extract a zip file in pure java (without using a library like zip4j).
     * The problem is that often some files are not copied. When there are several nested zip files, or sometimes empty folders, etc.
     */
    private fun extract() {

        UnzipUtils.unzip(dbConfig.downloadedMaria, dbConfig.installationDir)

//        val zipFile = ZipFile(dbConfig.downloadedMaria.toFile())
//        zipFile.isRunInThread = false
//
//        try {
//            zipFile.extractAll(dbConfig.installationDir.toRealPath().absolutePathString())
//        } catch (e: ZipException) {
//            e.printStackTrace()
//            return
//        }
    }

    private fun install() {
        dbConfig.dataDir.createDirectory()

        LOGGER.info("Running: mariadb-install-db.exe --datadir=${dbConfig.dataDir.toRealPath().absolutePathString()}")

        val args = listOf("cmd", "/c", "\"mariadb-install-db.exe\" --datadir=${dbConfig.dataDir.toRealPath().absolutePathString()}")

        val pb = ProcessBuilder(args)

        pb.directory(dbConfig.mariaDBFolder.resolve("bin").toFile())
        pb.redirectErrorStream(true)
        pb.environment()
        val process = pb.start()
        BufferedReader(InputStreamReader(process.inputStream)).use {
            val lines = mutableListOf<String>()
            while (true) {
                val line = it.readLine() ?: break
                lines.add(line)
                if (line == "Creation of the database was successful") lines.forEach(::println)
            }
        }

    }

    fun start() {
        if (!dbConfig.isRunInThread) startImpl()
        else launch {
            startImpl()
            startedCallback.accept(this@DB)
        }
    }

    private fun startImpl() {
        if (!isInstalled) {
            LOGGER.info("Unable to start the database server because the installation is not finished")
            return
        }

        if(isStarted){
            LOGGER.info("Database server has already been started")
            return
        }

        val latch = CountDownLatch(1)
        launch {

            LOGGER.info("[start, launch block] Thread name: ${Thread.currentThread().name}")
            LOGGER.info("Running: mysqld.exe --console --datadir=${
                withContext(Dispatchers.IO) {
                    dbConfig.dataDir.toRealPath()
                }.absolutePathString()} --port=${dbConfig.port}")

            val args = listOf("cmd", "/c", "\"mysqld.exe\" --console --datadir=${
                withContext(Dispatchers.IO) {
                    dbConfig.dataDir.toRealPath()
                }.absolutePathString()} --port=${dbConfig.port}")

            val pb = ProcessBuilder(args)
            pb.directory(dbConfig.mariaDBFolder.resolve("bin").toFile())
            pb.redirectErrorStream(true)
            pb.environment()

            val process = withContext(Dispatchers.IO) { pb.start() }

            BufferedReader(InputStreamReader(process.inputStream)).use {
                val lines = mutableListOf<String>()
                while (true) {
                    val line = it.readLine() ?: break

                    if (!isStarted) lines.add(line)
                    else LOGGER.info(line)

                    if (line.contains("mysqld") && line.contains("ready for connections.")) {
                        lines.forEach(LOGGER::info) // printing output for mysqld.exe
                        isStarted = true
                        latch.countDown()
                    }
                }
            }
        }
        latch.await()
    }

    fun stop() {
        LOGGER.info("Running command: mysqladmin.exe --user=root ---password=\"\" shutdown --port=${dbConfig.port}")

        val args = listOf("cmd", "/c", "\"mysqladmin.exe\" --user=root --password=\"\" shutdown --port=${dbConfig.port}")

        val pb = ProcessBuilder(args)
        pb.directory(dbConfig.mariaDBFolder.resolve("bin").toFile())
        pb.redirectErrorStream(true)
        pb.environment()

        BufferedReader(InputStreamReader(pb.start().inputStream)).use {
            while (true) { LOGGER.info(it.readLine() ?: break) }
        }
    }
}
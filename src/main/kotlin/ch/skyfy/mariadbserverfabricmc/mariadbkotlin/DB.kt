@file:Suppress("MemberVisibilityCanBePrivate")

package ch.skyfy.mariadbserverfabricmc.mariadbkotlin

import kotlinx.coroutines.*
import net.lingala.zip4j.ZipFile
import org.apache.commons.io.file.PathUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Suppress("unused")
class DB(
    private val dbConfig: DBConfig,
    var setupFilesCallback: Consumer<DB> = Consumer<DB> {},
    var startedCallback: Consumer<DB> = Consumer<DB> {},
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

    private val logger: Logger = LogManager.getLogger(DB::class)

    var isAlreadyInstalled = false
    var isSetupFilesDone = false
    var isStarted = false

    fun setupFiles() {

        logger.info("Setting up files ...")

        if (!dbConfig.isRunInThread) setupFilesImpl()
        else launch {
            setupFilesImpl()
            setupFilesCallback.accept(this@DB)
        }
    }

    private fun setupFilesImpl() {
        download()
        extract()
        install()
        isSetupFilesDone = true
    }

    private fun download() {
        val mariaDBFolder = Paths.get(dbConfig.mariaDBFolder)
        if (mariaDBFolder.exists()) {
            println("MariaDB is already installed here: ${mariaDBFolder.absolutePathString()}")
            isAlreadyInstalled = true
            return
        }

        createInstallationDir()

        FileOutputStream(dbConfig.mariaDBFolderAsZip).channel
            .transferFrom(Channels.newChannel(URL(DBConfig.VERSION[dbConfig.mariadbVersion]?.get(dbConfig.os)).openStream()), 0, Long.MAX_VALUE)
    }

    /**
     * I found a lot of examples to extract a zip file in pure java (without using a library like zip4j).
     * The problem is that often some files are not copied. When there are several nested zip files, or sometimes empty folders, etc.
     */
    private fun extract() {
        val zipFile = ZipFile(dbConfig.mariaDBFolderAsZip)
        zipFile.isRunInThread = false
        zipFile.extractAll(dbConfig.installationDir)
//        PathUtils.deleteFile(Paths.get(dbConfig.mariaDBFolderAsZip))
    }

    private fun install() {
        val datadir = Paths.get(dbConfig.dataDir)

        if (isAlreadyInstalled) {
            if (datadir.exists() && !PathUtils.isEmptyDirectory(datadir)) {
                println("The installation will not take place, because there is already a datadir folder containing files")
                return
            }
        }

        println("Running: mariadb-install-db.exe --datadir=${dbConfig.dataDir}")

        val args = listOf("cmd", "/c", "\"mariadb-install-db.exe\" --datadir=${dbConfig.dataDir}")

        val pb = ProcessBuilder(args)

        pb.directory(Paths.get(dbConfig.mariaDBFolder + "\\bin").toFile())
        pb.redirectErrorStream(true)
        pb.environment()
        val process = pb.start()
        BufferedReader(InputStreamReader(process.inputStream)).use {
            val lines = mutableListOf<String>()
            while (true) {
                val line = it.readLine() ?: break
                lines.add(line)
                if (line == "Creation of the database was successful") {
                    lines.forEach(::println)
                }
            }
        }

    }

    private fun createInstallationDir() {
        val path = Paths.get(dbConfig.installationDir)
        if (!path.exists()) path.createDirectories()
    }

    fun start() {
        if (!dbConfig.isRunInThread) startImpl()
        else launch {
            startImpl()
            startedCallback.accept(this@DB)
        }
    }

    private fun startImpl() {
        if (!isSetupFilesDone) {
            println("Database has not finished to setup the files !")
            return
        }

        val latch = CountDownLatch(1)
        launch {

            println("[start, launch block] Thread name: ${Thread.currentThread().name}")
            println("Running: mysqld.exe --console --datadir=${dbConfig.dataDir} --port=${dbConfig.port}")

            val args = listOf("cmd", "/c", "\"mysqld.exe\" --console --datadir=${dbConfig.dataDir} --port=${dbConfig.port}")

            val pb = ProcessBuilder(args)
            pb.directory(Paths.get(dbConfig.mariaDBFolder + "\\bin").toFile())
            pb.redirectErrorStream(true)
            pb.environment()

            val process = withContext(Dispatchers.IO) {
                pb.start()
            }

            BufferedReader(InputStreamReader(process.inputStream)).use {
                val lines = mutableListOf<String>()
                while (true) {
                    val line = it.readLine() ?: break

                    if (!isStarted) lines.add(line)
                    else println(line)

                    if (line.contains("mysqld") && line.contains("ready for connections.")) {
                        lines.forEach(::println) // printing output for mysqld.exe
                        isStarted = true
                        latch.countDown()
                    }
                }
            }
        }
        latch.await()
    }

    fun stop() {
        launch {
            println("Running command: mysqladmin.exe --user=root ---password=\"\" shutdown --port=${dbConfig.port}")

            val args = listOf("cmd", "/c", "\"mysqladmin.exe\" --user=root --password=\"\" shutdown --port=${dbConfig.port}")

            val pb = ProcessBuilder(args)
            pb.directory(Paths.get(dbConfig.mariaDBFolder + "\\bin").toFile())
            pb.redirectErrorStream(true)
            pb.environment()

            val process = withContext(Dispatchers.IO) { pb.start() }

            BufferedReader(InputStreamReader(process.inputStream)).use {
                while (true) {
                    val line = it.readLine() ?: break
                    println(line)
                }
            }
        }
    }

//    fun createDatabase(databaseName: String) {
//
//        async {
//
//            println("Running command: mysql.exe --user=root --password=\"\" --port=${dbConfig.port}")
//
////            val args = listOf("cmd", "/c", "\"mysql.exe\" --user=root --password=\"\" --port=${dbConfig.port}")
//            val args = listOf("\"mysql.exe\" --user=root --password=\"\" --port=${dbConfig.port}")
//
//            val pb = ProcessBuilder(args)
//            pb.directory(Paths.get(dbConfig.mariaDBFolder + "\\bin").toFile())
//            pb.redirectErrorStream(true)
//            pb.environment()
//            pb.inheritIO()
//            val process = withContext(Dispatchers.IO) { pb.start() }
//
//            BufferedReader(InputStreamReader(process.inputStream)).use {
//                while (true) {
//                    val line = it.readLine() ?: break
//                    println(line)
//                    if (line == "MariaDB [(none)]>") {
//                        BufferedWriter(OutputStreamWriter(process.outputStream)).use { bw ->
//                            bw.write("create database if not exists `$databaseName`; exit;")
//                            bw.flush()
//                        }
//                    }
//                }
//            }
//
//
//        }
//
//    }


}
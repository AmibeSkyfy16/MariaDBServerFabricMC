package ch.skyfy.mariadbserverfabricmc.prelaunch

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths
import java.util.*
import kotlin.coroutines.CoroutineContext


class MariaDBConsole(
    private val onReady: () -> Unit,
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

    val processBuilder: ProcessBuilder
    lateinit var process: Process

    var successStarted = false
    var ready = false

    init {
        processBuilder = ProcessBuilder(
//            *arrayOf("cmd.exe",
//                "/c",
//                "call",
////                "\"title\"",
////                "/D \"C:\\\\temp\"",
//                "\"mysqld.exe --console --port=3307\""
//            )

//            *arrayOf(
//                "./mysqld"
//            )

            *arrayOf(
                "E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin\\mysqld",
                "--console",
                "--port=3307"
//                ".\\mysqld"
            )

            // Works
//            *arrayOf(
//                "cmd.exe",
//                "/c",
//                "\"mysqld.exe --console --datadir=..\\data --port=3307\""
//            )


            // works
//            *arrayOf(
//                "cmd.exe",
//                "/c",
//                "call \"\"mysqld.exe --console --datadir=..\\data --port=3307\"\"",
////                "\"mysqld.exe --console --datadir=..\\data --port=3307\""
////                "\"mysqld.exe --console --datadir=..\\data --port=3307\""
//            )

            // Works
//            *arrayOf(
//                "cmd.exe",
//                "/c",
//                "start \"title\" \"\"mysqld.exe --console --datadir=..\\data --port=3307\"\"",
//            )

//            *arrayOf(
//                ".\\mysqld.exe", "--datadir=..\\data", "--port=3307"
//            )
        )
            .directory(Paths.get("E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin").toFile())
            .redirectErrorStream(true)

//            .inheritIO()
//            .redirectInput(ProcessBuilder.Redirect.PIPE)
//            .redirectOutput(ProcessBuilder.Redirect.PIPE)

//        pb2.environment().clear()
    }

    fun start() {
        launch {
            val scanner = Scanner(System.`in`)
//            var line2: String?
//            while (scanner.hasNext()) {
//                println(scanner.next())
//            }

//            var line2: String?
//            while (withContext(Dispatchers.IO) {
//                    System.`in`.bufferedReader().readLine()
//                }.also { line2 = it } != null) {
//                println("lline: $line2")
//            }

//            withContext(Dispatchers.IO) {
//                System.`in`.bufferedReader().readLine()
//            }

//            val p = Runtime.getRuntime().exec("C:\\Windows\\System32\\wbem\\WMIC.exe process where (processid=${process.pid()}) get parentprocessid")
//            val br = BufferedReader(InputStreamReader(p.inputStream))
//
//            var line2: String?
//            while (withContext(Dispatchers.IO) {
//                    br.readLine()
//                }.also { line2 = it } != null) {
//                println("lline: $line2")
//            }

//            while (true){
//                delay(100)
//                println("console -> " + System.console().readLine())
//            }

            println("\nChild exits.")
        }

        var checkingStatusCount = 0

        // Status check
        launch {
            while (true) {
                delay(200)
                val builder = ProcessBuilder(*arrayOf(
                        "E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin\\mysqladmin.exe",
                        "--user=root",
                        "--password=12345678",
                        "--port=3307",
                        "status"
                    ))
                    .directory(Paths.get("E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin").toFile())
                    .redirectErrorStream(true)

                val process = withContext(Dispatchers.IO) { builder.start() }

                BufferedReader(InputStreamReader(process.inputStream)).use { input ->
                    val successMessage = arrayOf("Uptime:", "Threads:", "Questions:") // means mysqld.exe is running
                    var l: String?
                    while (input.readLine().also { l = it } != null) {
                        l?.let { noNullLine ->
//                            println(noNullLine)
                            if (successMessage.all { noNullLine.contains(it) }) {
                                ready = true
                                onReady.invoke()
                            }
                        }
                    }
                }
                checkingStatusCount++
                if (ready) break
            }
        }

        @Suppress("ControlFlowWithEmptyBody")
        while (checkingStatusCount == 0) {
        } // Blocking the first time we are checking status of mysqld.exe

        println("ready is $ready")
        if (ready) return

//        val p = ProcessHandle.allProcesses().filter { it.pid() == process.pid() }.findFirst().get()
//        Runtime.getRuntime().exec()

//        launch {

        process = processBuilder.start()

        BufferedReader(InputStreamReader(process.inputStream)).use { input ->
            val successMessage = "ready for connections"
            val readyKeyWords = arrayOf("Version:", "socket:", "port:") // Last printed line by mysqld.exe
            var line: String?
            while (input.readLine().also { line = it } != null) {
                line?.let { noNullLine ->
//                    println("$this" + noNullLine)
                    if (noNullLine.contains(successMessage)) successStarted = true
                    if (successStarted && readyKeyWords.all { keyWord -> noNullLine.contains(keyWord) }) ready = true
                    if (ready) onReady.invoke()
                }
            }
        }

        process.waitFor()

    }

}
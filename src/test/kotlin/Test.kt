import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBConsole
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths
import kotlin.test.Test

class Test {

    @Test
    fun test() {
        if (0 == 0) return
        println("test")

        var startMariaDB = true

//        val dataDir = "--datadir=\"E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\data\""
//        val password = "--password=12345678"
//        val pb = ProcessBuilder(*arrayOf("cmd.exe", "/c", "mariadb-install-db.exe $dataDir $password"))
//            .directory(Paths.get("E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin").toFile())
//        val process = pb.start()
//
//        val successMessage = "Creation of the database was successful"
//        BufferedReader(InputStreamReader(process.inputStream)).use { input ->
//            var line: String?
//            while (input.readLine().also { line = it } != null) {
//                line?.let {
//                    println(it)
//                    if (it == successMessage) {
//                        startMariaDB = true
//                    }
//                }
//            }
//        }

        if (startMariaDB) {
//            val pb2 = ProcessBuilder(*arrayOf("cmd.exe", "/c", "start", "mysqld.exe --console --port=3307"))
//                .directory(Paths.get("E:\\tmp\\minecraft servers\\fabricmc__1.19.3_0.14.17__0.11.1\\server\\config\\mariadb_server_fabricmc\\mariadb-10.10.2\\bin").toFile())
//            val process2 = pb2.start()

            MariaDBConsole(onReady = {
                println("Mariadb is ready")
            }).start()

            Thread.sleep(100000)
        }

    }

}
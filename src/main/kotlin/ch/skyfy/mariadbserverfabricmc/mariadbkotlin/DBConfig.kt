package ch.skyfy.mariadbserverfabricmc.mariadbkotlin

import org.apache.commons.lang3.SystemUtils
import java.nio.file.Path
import java.nio.file.Paths

@Suppress("unused")
class DBConfig private constructor(
    val port: Int,
    val mariadbVersion: MariaDBVersion,
    val installationDir: Path,
    var downloadedMaria: Path,
    var mariaDBFolder: Path,
    var dataDir: Path,
    val os: OS,
    val isRunInThread: Boolean
) {

    enum class OS {
        WINDOWS,
        LINUX
    }

    enum class MariaDBVersion(val filename: String) {
        STABLE_10_8_3("mariadb-10.8.3-winx64.zip")
    }

    companion object {
        val VERSION = mapOf(
            MariaDBVersion.STABLE_10_8_3 to mapOf(
                OS.WINDOWS to "https://dlm.mariadb.com/2314696/MariaDB/mariadb-10.8.3/winx64-packages/mariadb-10.8.3-winx64.zip",
                OS.LINUX to "https://dlm.mariadb.com/2314683/MariaDB/mariadb-10.8.3/bintar-linux-systemd-x86_64/mariadb-10.8.3-linux-systemd-x86_64.tar.gz"
            )
        )
    }

    data class Builder(
        var port: Int = 3306,
        var mariaDBVersion: MariaDBVersion = MariaDBVersion.STABLE_10_8_3,
        var installationDir: Path = Paths.get(SystemUtils.JAVA_IO_TMPDIR + "/EmbeddedMariaDB"),
        var downloadedMaria: Path = installationDir.resolve(mariaDBVersion.filename),
        var mariaDBFolder: Path = installationDir.resolve(downloadedMaria.fileName.toString().replace("\\.\\w+$".toRegex(), "")),
        var dataDir: Path = mariaDBFolder.resolve("data"),
        var os: OS = if (SystemUtils.IS_OS_WINDOWS) OS.WINDOWS else OS.LINUX,
        var isRunInThread: Boolean = false
    ) {
        fun port(port: Int) = apply { this.port = port }
        fun mariaDBVersion(mariaDBVersion: MariaDBVersion) = apply { this.mariaDBVersion = mariaDBVersion }
        fun installationDir(installationDir: Path) = apply { this.installationDir = installationDir }
        fun mariaDBFolder(mariaDBFolder: Path) = apply { this.mariaDBFolder = mariaDBFolder }
        fun dataDir(dataDir: Path) = apply { this.dataDir = dataDir }
        fun os(os: OS) = apply { this.os = os }
        fun isRunInThread(isRunInThread: Boolean) = apply { this.isRunInThread = isRunInThread }
        fun build() = DBConfig(port, mariaDBVersion, installationDir, downloadedMaria,mariaDBFolder, dataDir, os, isRunInThread)
    }

}
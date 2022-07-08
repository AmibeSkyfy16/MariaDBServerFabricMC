package ch.skyfy.mariadbserverfabricmc.mariadbkotlin

import org.apache.commons.lang3.SystemUtils

@Suppress("unused")
class DBConfig private constructor(
    val port: Int,
    val mariadbVersion: MariaDBVersion,
    val installationDir: String,
    var mariaDBFolder: String,
    var mariaDBFolderAsZip: String,
    var dataDir: String,
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
        var installationDir: String = SystemUtils.JAVA_IO_TMPDIR + "/EmbeddedMariaDB",
        var mariaDBFolderAsZip: String = installationDir + "/${mariaDBVersion.filename}",
        var mariaDBFolder: String = mariaDBFolderAsZip.replace("\\.\\w+$".toRegex(), ""),
        var dataDir: String = "$mariaDBFolder/data",
        var os: OS = if (SystemUtils.IS_OS_WINDOWS) OS.WINDOWS else OS.LINUX,
        var isRunInThread: Boolean = false
    ) {
        fun port(port: Int) = apply { this.port = port }
        fun mariaDBVersion(mariaDBVersion: MariaDBVersion) = apply { this.mariaDBVersion = mariaDBVersion }
        fun installationDir(installationDir: String) = apply { this.installationDir = installationDir }
        fun mariaDBFolder(mariaDBFolder: String) = apply { this.mariaDBFolder = mariaDBFolder }
        fun dataDir(dataDir: String) = apply { this.dataDir = dataDir }
        fun os(os: OS) = apply { this.os = os }
        fun isRunInThread(isRunInThread: Boolean) = apply { this.isRunInThread = isRunInThread }
        fun build() = DBConfig(port, mariaDBVersion, installationDir, mariaDBFolder, mariaDBFolderAsZip, dataDir, os, isRunInThread)
    }

}
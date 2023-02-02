package ch.skyfy.mariadbserverfabricmc.utils

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

fun setupConfigDirectory() {
    try {
        if (!MariaDBServerFabricMCMod.CONFIG_DIRECTORY.exists()) MariaDBServerFabricMCMod.CONFIG_DIRECTORY.createDirectory()
    } catch (e: java.lang.Exception) {
        MariaDBServerFabricMCMod.LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
        throw RuntimeException(e)
    }
}

//fun setupMariaDBDirectory() {
//    try {
//        if (!MariaDBServerFabricMCMod.MARIADB_DIRECTORY.exists()) MariaDBServerFabricMCMod.MARIADB_DIRECTORY.createDirectory()
//    } catch (e: java.lang.Exception) {
//        MariaDBServerFabricMCMod.LOGGER.fatal("An exception occurred. Could not create the extension folder that should contain the extensions files")
//        throw RuntimeException(e)
//    }
//}
//
//fun setupDataDirectory() {
//    try {
//        if (!MariaDBServerFabricMCMod.DATA_DIRECTORY.exists()) MariaDBServerFabricMCMod.DATA_DIRECTORY.createDirectory()
//    } catch (e: java.lang.Exception) {
//        MariaDBServerFabricMCMod.LOGGER.fatal("An exception occurred. Could not create the extension folder that should contain the extensions files")
//        throw RuntimeException(e)
//    }
//}
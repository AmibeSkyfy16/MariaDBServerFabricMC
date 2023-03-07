package ch.skyfy.mariadbserverfabricmc.utils

import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.CONFIG_DIRECTORY
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher.Companion.LOGGER
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

fun setupConfigDirectory() {
    try {
        if (!CONFIG_DIRECTORY.exists()) CONFIG_DIRECTORY.createDirectory()
    } catch (e: java.lang.Exception) {
        LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
        throw RuntimeException(e)
    }
}
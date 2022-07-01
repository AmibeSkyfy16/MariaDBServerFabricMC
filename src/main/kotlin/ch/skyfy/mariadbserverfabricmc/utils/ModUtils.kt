package ch.skyfy.mariadbserverfabricmc.utils

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod.Companion.CONFIG_DIRECTORY
import java.io.File

object ModUtils {
    fun getRelativeFile(relativeFilePath: String): File {
        return CONFIG_DIRECTORY.resolve(relativeFilePath).toFile()
    }
}
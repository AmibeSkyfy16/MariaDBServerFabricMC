package ch.skyfy.mariadbserverfabricmc.utils

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod.Companion.CONFIG_DIRECTORY
import java.nio.file.Path

object ModUtils {
    fun getRelativeFileAsPath(relativeFilePath: String): Path = CONFIG_DIRECTORY.resolve(relativeFilePath)

}
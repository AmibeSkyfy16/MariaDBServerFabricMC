package ch.skyfy.mariadbserverfabricmc.utils

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod

object ReflectionUtils {
    fun loadClassesByReflection(classesToLoad: Array<Class<*>>) {
        for (config in classesToLoad) {
            val canonicalName = config.canonicalName
            try {
                Class.forName(canonicalName)
            } catch (e: ClassNotFoundException) {
                MariaDBServerFabricMCMod.LOGGER.fatal("A FATAL ERROR OCCURRED WITH FKMod")
                MariaDBServerFabricMCMod.LOGGER.fatal("A FATAL ERROR OCCURRED WITH FKMod")
                MariaDBServerFabricMCMod.LOGGER.fatal("A FATAL ERROR OCCURRED WITH FKMod")
                MariaDBServerFabricMCMod.LOGGER.fatal("A FATAL ERROR OCCURRED WITH FKMod")
//                throw MariaDBServerFabricMCModException(e)
            }
        }
    }
}
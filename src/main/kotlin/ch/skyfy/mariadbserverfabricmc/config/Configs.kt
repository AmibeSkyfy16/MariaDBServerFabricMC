package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.json5configlib.ConfigData
import ch.skyfy.json5configlib.ConfigManager
import ch.skyfy.mariadbserverfabricmc.prelaunch.MariaDBServerFabricMCModPreLauncher

object Configs {

    val MARIADB_INSTALLATION_PROGRESS = ConfigData.invokeSpecial<MariaDBInstallationProgress>(MariaDBServerFabricMCModPreLauncher.CONFIG_DIRECTORY.resolve("mariadb_installation_progress.json5"), true)
    val DB_CONFIG = ConfigData.invokeSpecial<DBConfig>(MariaDBServerFabricMCModPreLauncher.CONFIG_DIRECTORY.resolve("db_config.json5"), true)

}
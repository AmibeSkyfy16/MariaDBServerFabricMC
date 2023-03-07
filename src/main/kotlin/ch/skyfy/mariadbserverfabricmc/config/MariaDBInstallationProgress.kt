package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class MariaDBInstallationProgress(
    var copyMariadbZipFromAssetToConfigFolderSuccess: Boolean = false,
    var mariaInstallSuccess: Boolean = false,
    var mariaStarted: Boolean = false,
) : Validatable
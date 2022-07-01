package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.mariadbserverfabricmc.json.Validatable

data class ModConfig(
    val port: Int,
    val dataDir: String?,
    val baseDir: String?
) : Validatable {
    override fun validate() {

    }

    override fun validatePrimitivesType(errors: MutableList<String?>?) {

    }
}
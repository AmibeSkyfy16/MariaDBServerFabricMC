package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.mariadbserverfabricmc.json.Validatable

data class ModConfig(val port: Int, val dataDir: String?, val baseDir: String?) : Validatable {
    override fun validate() {
        validatePrimitivesType(mutableListOf())
    }

    override fun validatePrimitivesType(errors: MutableList<String?>?) {
        // TODO The TCP/IP port should be between 1 024 - 65 535
    }
}
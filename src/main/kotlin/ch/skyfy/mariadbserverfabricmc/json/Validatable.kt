@file:Suppress("unused")

package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.MariaDBServerFabricMCMod
import ch.skyfy.mariadbserverfabricmc.exceptions.MariaDBServerFabricMCModException
import ch.skyfy.mariadbserverfabricmc.utils.ValidateUtils

interface Validatable {

    /**
     * A typo or a mistake can happen quickly.
     * If this is what happened when the user was setting up the files it could have bad repercussions on the game
     *
     * This should not happen at all
     */
    fun validate(){}

    /**
     * Check that the primitive type values are correct.
     * Ex: "port (for the mariadb server connection" can't be less than 0 or greater than 65535
     */
    fun validatePrimitivesType(errors: MutableList<String?>?){}

    /**
     * Performs a thorough check of all fields in a class to make sure nothing is null
     * This method must be called at the very beginning of validate
     */
    fun validateNonNull(errors: MutableList<String?>) {
        try {
            ValidateUtils.traverse(this, errors)
        } catch (e: NullPointerException) {
            errors.forEach(MariaDBServerFabricMCMod.LOGGER::fatal)
            MariaDBServerFabricMCMod.LOGGER.fatal("FATAL ERROR")
            throw MariaDBServerFabricMCModException(e)
        }
    }

}
package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.utils.ModUtils

data class JsonData<DATA : Validatable>(val data: DATA){
    companion object{
        inline operator fun <reified DATA : Validatable, reified DEFAULT : Defaultable<DATA>> invoke(relativeFilePath: String) : JsonData<DATA> =
             JsonData(JsonManager.getOrCreateConfig<DATA, DEFAULT>(ModUtils.getRelativeFileAsPath(relativeFilePath)))
    }
}

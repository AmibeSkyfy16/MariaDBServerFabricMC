package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.utils.ModUtils
import java.nio.file.Path

data class JsonData<DATA : Validatable>(val data: DATA, val relativeFilePath: Path){
    companion object{
        inline operator fun <reified DATA : Validatable, reified DEFAULT : Defaultable<DATA>> invoke(relativeFilePath: String) : JsonData<DATA> =
             JsonData(JsonManager.getOrCreateConfig<DATA, DEFAULT>(ModUtils.getRelativeFileAsPath(relativeFilePath)), ModUtils.getRelativeFileAsPath(relativeFilePath))
    }
}

package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.utils.ModUtils
import kotlin.reflect.KClass

data class JsonData<DATA : Validatable, DEFAULT : Defaultable<DATA>>(
    val relativeFilePath: String,
    val dataClass: KClass<out Validatable>,
    val dataClass2: KClass<DATA>,
    val defaultConfigClass: KClass<out Defaultable<DATA>>
){

    private var jsonManager: JsonManager<DATA, DEFAULT> = JsonManager(
        file = ModUtils.getRelativeFile(relativeFilePath),
        dataClass = dataClass,
        dataClass2 = dataClass2,
        defaultConfigClass = defaultConfigClass)

    private var data: DATA = jsonManager.getOrCreateConfig()

}

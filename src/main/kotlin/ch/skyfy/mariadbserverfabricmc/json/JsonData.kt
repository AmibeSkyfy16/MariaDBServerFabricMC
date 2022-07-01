package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.utils.ModUtils
import com.google.gson.GsonBuilder
import kotlin.reflect.KClass

data class JsonData<DATA : Validatable, DEFAULT : Defaultable<DATA>>(
    val relativeFilePath: String,
    val dataClass: KClass<out Validatable>,
    val dataClass2: KClass<DATA>,
    val defaultConfigClass: KClass<out Defaultable<DATA>>
){

//    companion object {
//        inline fun <reified DATA : Validatable,reified DEFAULT : Defaultable<DATA>> create(file: File)
//                = JsonManager(GsonBuilder().setPrettyPrinting().serializeNulls().create(), file, DATA::class, DEFAULT::class)
//    }

    private var jsonManager: JsonManager<DATA, DEFAULT> = JsonManager(GsonBuilder().setPrettyPrinting().serializeNulls().create(),
        ModUtils.getRelativeFile(relativeFilePath), dataClass,dataClass2, defaultConfigClass)
    private var `data`: DATA = jsonManager.getOrCreateConfig2()

//    private inline fun <reified DATA : Validatable, DEFAULT : Defaultable<DATA>> getData() : DATA{
//        return jsonManager.getOrCreateConfig2()
//    }

}

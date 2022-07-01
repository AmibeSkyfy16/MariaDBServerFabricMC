@file:Suppress("UnstableApiUsage")

package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.exceptions.MariaDBServerFabricMCModException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


class JsonManager<DATA, DEFAULT>(
    private val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create(),
    private val file: File,
    private val dataClass: KClass<out Validatable>,
    private val dataClass2: KClass<DATA>,
    private val defaultConfigClass: KClass<out Defaultable<DATA>>
) where DATA : Validatable, DEFAULT : Defaultable<DATA> {

    fun getOrCreateConfig(): DATA {
        val config: DATA
        try {
            if (file.exists()) config = get()
            else {
                config = defaultConfigClass::createInstance.invoke().getDefault()

//                val actualRuntimeClassConstructor = defaultConfigClass::class.constructors.first()
//                val defaultConfig = actualRuntimeClassConstructor.call()
//                val d = defaultConfig.safeCast(defaultConfigClass)
//                config = d?.getDefault()!!
//                config = defaultConfigClass.getDeclaredConstructor().newInstance()?.getDefault()!!
                save(config)
            }
        } catch (e: java.lang.Exception) {
            throw MariaDBServerFabricMCModException(e)
        }
        return config
    }

    @Throws(IOException::class)
    fun get(): DATA {
        FileReader(file).use { reader -> return gson.fromJson(reader, dataClass2.java) }
    }

    @Throws(IOException::class)
    fun save(data: DATA) {
        file.parentFile.mkdirs()
        FileWriter(file).use { writer -> gson.toJson(data, dataClass.java, writer) }
    }
}
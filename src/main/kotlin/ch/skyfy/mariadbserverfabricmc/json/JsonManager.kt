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


class JsonManager<DATA : Validatable, DEFAULT : Defaultable<DATA>>(
    private val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create(),
    private val file: File,
    private val dataClass: KClass<out Validatable>,
    private val dataClass2: KClass<DATA>,
    private val defaultConfigClass: KClass<out Defaultable<DATA>>
) {

    fun getOrCreateConfig(): DATA = try {
        if (file.exists()) get()
        else save(defaultConfigClass::createInstance.invoke().getDefault())
    } catch (e: java.lang.Exception) {
        throw MariaDBServerFabricMCModException(e)
    }


    @Throws(IOException::class)
    fun get(): DATA {
        FileReader(file).use { reader -> return gson.fromJson(reader, dataClass2.java) }
    }

    @Throws(IOException::class)
    fun save(config: DATA): DATA {
        file.parentFile.mkdirs()
        FileWriter(file).use { writer -> gson.toJson(config, dataClass.java, writer) }
        return config
    }
}
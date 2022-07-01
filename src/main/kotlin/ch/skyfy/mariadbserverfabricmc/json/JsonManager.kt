@file:Suppress("UnstableApiUsage")

package ch.skyfy.mariadbserverfabricmc.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.safeCast
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.reflect

class JsonManager<DATA , DEFAULT>(
    val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create(),
    val file: File,
    val dataClass: KClass<out Validatable>,
    val dataClass2: KClass<DATA>,
    val defaultConfigClass: KClass<out Defaultable<DATA>>
) where DATA : Validatable, DEFAULT : Defaultable<DATA> {

     fun getOrCreateConfig2(): DATA {
        val config: DATA
        try {
            if (file.exists()) config = get()
            else {

                val t1 = defaultConfigClass::createInstance
                val default = t1.invoke()
                config = default.getDefault()

//                val actualRuntimeClassConstructor = defaultConfigClass::class.constructors.first()
//                val defaultConfig = actualRuntimeClassConstructor.call()
//                val d = defaultConfig.safeCast(defaultConfigClass)
//                config = d?.getDefault()!!
//                config = defaultConfigClass.getDeclaredConstructor().newInstance()?.getDefault()!!
                save(config)
            }
        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
        return config
    }

    @Throws(IOException::class)
      fun get(): DATA {

        FileReader(file).use {
                reader -> return gson.fromJson(reader, dataClass2.java)
        }
    }

    @Throws(IOException::class)
     fun save(data: DATA) {
        file.parentFile.mkdirs()
        FileWriter(file).use { writer -> gson.toJson(data, dataClass.java, writer) }
    }
}
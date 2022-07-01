@file:Suppress("UnstableApiUsage")

package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.exceptions.MariaDBServerFabricMCModException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import kotlin.reflect.full.createInstance

object JsonManager{

    inline fun <reified DATA : Validatable, reified DEFAULT : Defaultable<DATA>> getOrCreateConfig(
        file: File,
        gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    ): DATA = try {
        if (file.exists()) get(file, gson)
        else save(DEFAULT::class.createInstance().getDefault(), file, gson)
    } catch (e: java.lang.Exception) {
        throw MariaDBServerFabricMCModException(e)
    }

    @Throws(IOException::class)
    inline fun <reified DATA : Validatable> get(file: File, gson: Gson): DATA {
        FileReader(file).use { reader -> return gson.fromJson(reader, DATA::class.java) }
    }

    @Throws(IOException::class)
    inline fun <reified DATA : Validatable> save(config: DATA, file: File, gson: Gson): DATA {
        file.parentFile.mkdirs()
        FileWriter(file).use { writer -> gson.toJson(config, DATA::class.java, writer) }
        return config
    }
}
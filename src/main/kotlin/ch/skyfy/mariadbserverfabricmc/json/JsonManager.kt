@file:Suppress("UnstableApiUsage")

package ch.skyfy.mariadbserverfabricmc.json

import ch.skyfy.mariadbserverfabricmc.exceptions.MariaDBServerFabricMCModException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.reflect.full.createInstance

object JsonManager{

    inline fun <reified DATA : Validatable, reified DEFAULT : Defaultable<DATA>> getOrCreateConfig(
        file: Path,
        gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    ): DATA = try {
        if (file.exists()) get(file, gson)
        else save(DEFAULT::class.createInstance().getDefault(), file, gson)
    } catch (e: java.lang.Exception) {
        throw MariaDBServerFabricMCModException(e)
    }

    @Throws(IOException::class)
    inline fun <reified DATA : Validatable> get(file: Path, gson: Gson): DATA =
        Files.newBufferedReader(file).use { reader -> return gson.fromJson(reader, DATA::class.java) }

    @Throws(IOException::class)
    inline fun <reified DATA : Validatable> save(config: DATA, file: Path, gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()): DATA {
        file.parent.createDirectories()
        Files.newBufferedWriter(file).use { writer -> gson.toJson(config, DATA::class.java, writer) }
        return config
    }
}
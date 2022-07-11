package ch.skyfy.mariadbserverfabricmc.utils

import java.io.*
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object UnzipUtils {

    @Throws(IOException::class)
    fun unzip(zipFilePath: Path, destDirectory: Path) {
        if(!destDirectory.exists()) destDirectory.createDirectories()

        ZipFile(zipFilePath.absolutePathString()).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory.resolve(entry.name)
                    if (!entry.isDirectory) extractFile(input, filePath)
                    else filePath.createDirectories()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: Path) {
        BufferedOutputStream(FileOutputStream(destFilePath.absolutePathString())).use {bos ->
            var read: Int
            val bytesIn = ByteArray(BUFFER_SIZE)
            while (inputStream.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }

    /**
     * Size of the buffer to read/write data
     */
    private const val BUFFER_SIZE = 4096

}
package de.stephanlindauer.criticalmaps.model

import android.os.StatFs
import java.io.File

class StorageLocation(path: File) {
    private val dbFile: File

    init {
        require(path.exists()) { "Path does not exist or is read only: $path" }
        dbFile = File(path, "mbgl-offline.db")
    }

    val freeSpaceBytes: Long
        get() = StatFs(dbFile.absolutePath).availableBytes

    val totalSizeBytes: Long
        get() = StatFs(dbFile.absolutePath).totalBytes

    val usedSpace: Long
        get() = totalSizeBytes - freeSpaceBytes

    val cacheSize: Long
        get() = if (dbFile.exists()) dbFile.length() else 0
}

package ch.skyfy.mariadbserverfabricmc.callbacks

import ch.skyfy.mariadbserverfabricmc.EmbeddedDatabase
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.util.ActionResult

fun interface MariaDBInstalledCallback {
    companion object {
        @JvmField
        val EVENT: Event<MariaDBInstalledCallback> = EventFactory.createArrayBacked(MariaDBInstalledCallback::class.java) { listeners ->
            MariaDBInstalledCallback { embeddedDatabase ->
                for (listener in listeners)
                    listener.onInstalled(embeddedDatabase)
            }
        }
    }

    fun onInstalled(embeddedDatabase: EmbeddedDatabase)
}
package ch.skyfy.mariadbserverfabricmc.api

import ch.skyfy.mariadbserverfabricmc.EmbeddedDatabase
import ch.vorburger.mariadb4j.DB

@Suppress("MemberVisibilityCanBePrivate")
object EmbeddedDatabaseAPI {

    val db: DB

    init {
        val embeddedDatabase = EmbeddedDatabase()
        db = embeddedDatabase.db
        embeddedDatabase.startMariaDBServer()
    }

}
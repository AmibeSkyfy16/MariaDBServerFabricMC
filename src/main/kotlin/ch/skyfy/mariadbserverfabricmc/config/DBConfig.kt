package ch.skyfy.mariadbserverfabricmc.config

import ch.skyfy.json5configlib.Validatable
import kotlinx.serialization.Serializable

@Serializable
data class DBConfig(
    val port: Int = 3307,
    val password: String = "12345678"
) : Validatable
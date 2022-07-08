package ch.skyfy.mariadbserverfabricmc.persistants

import ch.skyfy.mariadbserverfabricmc.json.Validatable

data class PersistantData(var downloadStatus: Status , var extractStatus: Status, var installStatus: Status) : Validatable
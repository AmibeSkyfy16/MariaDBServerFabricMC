package ch.skyfy.mariadbserverfabricmc.persistants

import ch.skyfy.mariadbserverfabricmc.json.Defaultable
import ch.skyfy.mariadbserverfabricmc.json.JsonData

object Persistants {

    val PERSISTANT_DATA: JsonData<PersistantData> = JsonData.invoke<PersistantData, ModConfigDefault>("persistants.json")

    class ModConfigDefault : Defaultable<PersistantData> {
        override fun getDefault(): PersistantData = PersistantData(Status.FAILED, Status.FAILED, Status.FAILED)
    }

}
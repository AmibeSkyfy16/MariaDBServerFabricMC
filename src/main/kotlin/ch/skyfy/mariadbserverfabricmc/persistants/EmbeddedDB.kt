package ch.skyfy.mariadbserverfabricmc.persistants

data class EmbeddedDB(
    var downloadStatus: Pair<String, Status> = Pair("", Status.FAILED),
    var extractStatus: Pair<String, Status> = Pair("", Status.FAILED),
    var installStatus: Pair<String, Status> = Pair("", Status.FAILED),
) {

}
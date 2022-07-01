package ch.skyfy.mariadbserverfabricmc.json

interface Defaultable<DATA> {
    fun getDefault(): DATA
}
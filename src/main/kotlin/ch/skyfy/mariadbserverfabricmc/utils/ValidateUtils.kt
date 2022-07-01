package ch.skyfy.mariadbserverfabricmc.utils

import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
object ValidateUtils {

    fun traverse(obj: Any, errors: MutableList<String?>) {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            if (clazz.isPrimitive || !clazz.getPackage().name.startsWith("ch.skyfy")) return
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                try {
                    val next = field[obj]
                    val msg = field.name + " of class " + clazz.canonicalName + " should not be null"
                    if (next == null) errors.add(msg)
                    Objects.requireNonNull(next, msg)
                    traverse(next, errors)
                } catch (ignored: IllegalAccessException) {
                }
            }
            clazz = clazz.superclass
        }
    }

}
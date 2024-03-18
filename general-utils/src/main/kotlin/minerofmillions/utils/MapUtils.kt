package minerofmillions.utils

fun <K, V> Map<K, MutableCollection<V>>.removeFromAll(element: V) = values.forEach { it.remove(element) }

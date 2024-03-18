@file:OptIn(ExperimentalTypeInference::class)

package minerofmillions.utils

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.ExperimentalTypeInference

fun <T> synchronizedListOf(vararg elements: T): MutableList<T> =
    Collections.synchronizedList(mutableListOf(elements = elements))

fun <T> synchronizedSetOf(vararg elements: T): MutableSet<T> =
    ConcurrentHashMap.newKeySet<T>().apply { addAll(elements) }

fun <K, V> synchronizedMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> =
    ConcurrentHashMap<K, V>().apply { putAll(pairs) }

fun <T> Collection<T>.contentsEqual(other: Collection<T>) = all(other::contains) && other.all(::contains)
fun <T> Collection<T>.contentsEqualOrdered(other: Collection<T>) =
    size == other.size && zip(other).all { (first, second) -> first == second }

@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.productOf(transform: (T) -> Int): Int = fold(1) { acc, t -> acc * transform(t) }

@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.productOf(transform: (T) -> Long): Long = fold(1L) { acc, t -> acc * transform(t) }

@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.productOf(transform: (T) -> Float): Float = fold(1f) { acc, t -> acc * transform(t) }

@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.productOf(transform: (T) -> Double): Double = fold(1.0) { acc, t -> acc * transform(t) }

fun <T> Collection<T>.onlyOrNull() = if (size == 1) first() else null
fun <T> Collection<T>.onlyOrNull(predicate: (T) -> Boolean) = if (count(predicate) == 1) first(predicate) else null

fun <T> Collection<T>.only() = onlyOrNull() ?: throw IllegalStateException("Collection doesn't have exactly 1 element.")
fun <T> Collection<T>.only(predicate: (T) -> Boolean) =
    onlyOrNull(predicate) ?: throw IllegalStateException("Collection doesn't have exactly 1 matching element.")

fun <K, V: Any, R: MutableMap<K, V>> Iterable<K>.associateWithNotNullTo(result: R, transform: (K) -> V?): R {
    forEach { key ->
        transform(key)?.let {
            result[key] = it
        }
    }
    return result
}

fun <K, V: Any> Iterable<K>.associateWithNotNull(transform: (K) -> V?): Map<K, V> = associateWithNotNullTo(mutableMapOf(), transform)
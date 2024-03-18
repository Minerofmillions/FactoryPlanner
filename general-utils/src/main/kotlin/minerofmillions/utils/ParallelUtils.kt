package minerofmillions.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
val dispatcher = Dispatchers.IO.limitedParallelism(Runtime.getRuntime().availableProcessors() - 1)

suspend inline fun <T> Iterable<T>.forEachParallel(crossinline action: (T) -> Unit) {
    coroutineScope {
        forEach { launch { action(it) } }
    }
}

suspend inline fun <T> Sequence<T>.forEachParallel(crossinline action: (T) -> Unit) {
    coroutineScope {
        forEach { launch { action(it) } }
    }
}

suspend inline fun <T> Array<T>.forEachParallel(crossinline action: (T) -> Unit) {
    coroutineScope {
        forEach { launch { action(it) } }
    }
}

suspend inline fun <K, V> Map<K, V>.forEachParallel(crossinline action: (Map.Entry<K, V>) -> Unit) {
    coroutineScope {
        forEach { launch { action(it) } }
    }
}

suspend inline fun <T, R, C : MutableCollection<R>> Iterable<T>.mapParallelTo(
    output: C,
    crossinline transform: (T) -> R
): C {
    forEachParallel { output.add(transform(it)) }
    return output
}

suspend inline fun <T, R, C : MutableCollection<R>> Array<T>.mapParallelTo(
    output: C,
    crossinline transform: (T) -> R
) : C {
    forEachParallel { output.add(transform(it)) }
    return output
}

suspend inline fun <T, R> Iterable<T>.mapParallel(crossinline transform: (T) -> R): List<R> =
    mapParallelTo(synchronizedListOf(), transform)

suspend inline fun <T, R> Array<T>.mapParallel(crossinline transform: (T) -> R): List<R> =
    mapParallelTo(synchronizedListOf(), transform)

suspend inline fun <T, R> Sequence<T>.mapParallel(crossinline transform: (T) -> R): Sequence<R> = toList().mapParallel(transform).asSequence()

package minerofmillions.utils

fun <A, B> Pair<A?, B?>.extractNull(): Pair<A, B>? = if (first == null || second == null) null else first!! to second!!

inline fun <A, B> Iterable<Pair<A, B>>.forEach(action: (A, B) -> Unit) = forEach { (a, b) -> action(a, b) }
inline fun <A, B> Sequence<Pair<A, B>>.forEach(action: (A, B) -> Unit) = forEach { (a, b) -> action(a, b) }

inline fun <R, A, B> Iterable<R>.mapPairNotNull(crossinline transform: (R) -> Pair<A?, B?>) =
    mapNotNull { transform(it).extractNull() }

inline fun <R, A, B> Sequence<R>.mapPairNotNull(crossinline transform: (R) -> Pair<A?, B?>) =
    mapNotNull { transform(it).extractNull() }

package minerofmillions.utils

import java.io.Closeable

fun <E> identity(it: E) = it

fun <E> ((E) -> Boolean).not() = { e: E -> !invoke(e) }

operator fun <P1, P2, V> ((P1, P2) -> V).invoke(p2: P2) = { p1: P1 -> invoke(p1, p2) }

inline fun <T : Closeable, R> T.runUsing(block: T.() -> R) = use { it.block() }

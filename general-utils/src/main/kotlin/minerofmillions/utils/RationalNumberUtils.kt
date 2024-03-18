@file:OptIn(ExperimentalTypeInference::class)

package minerofmillions.utils

import org.ojalgo.scalar.RationalNumber
import java.math.BigDecimal
import kotlin.experimental.ExperimentalTypeInference

private val EPSILON = rationalOf(1, 1_000_000)

@OverloadResolutionByLambdaReturnType
inline fun <E> Iterable<E>.sumOf(transform: (E) -> RationalNumber): RationalNumber =
    fold(RationalNumber.ZERO) { acc, e -> acc + transform(e) }

@OverloadResolutionByLambdaReturnType
inline fun <E> Iterable<E>.productOf(transform: (E) -> RationalNumber): RationalNumber =
    fold(RationalNumber.ONE) { acc, e -> acc * transform(e) }

fun rationalOf(n: Int, d: Int = 1): RationalNumber = RationalNumber.of(n.toLong(), d.toLong())
fun rationalOf(n: Long, d: Long = 1): RationalNumber = RationalNumber.of(n, d)

operator fun RationalNumber.plus(other: RationalNumber): RationalNumber =
    add(other)

@JvmName("rationalPlusNullable")
operator fun RationalNumber?.plus(other: RationalNumber?): RationalNumber? =
    if (this == null) other
    else if (other == null) this
    else add(other)

operator fun RationalNumber.minus(other: RationalNumber): RationalNumber =
    subtract(other)

operator fun RationalNumber.times(other: RationalNumber): RationalNumber =
    multiply(other)

operator fun RationalNumber.div(other: RationalNumber): RationalNumber =
    divide(other)

operator fun RationalNumber.rem(n: RationalNumber): RationalNumber =
    RationalNumber.valueOf(toBigDecimal() % n.toBigDecimal())

operator fun RationalNumber.plus(other: Int): RationalNumber =
    add(rationalOf(other))

operator fun RationalNumber.minus(other: Int): RationalNumber =
    subtract(rationalOf(other))

operator fun RationalNumber.times(other: Int): RationalNumber =
    multiply(rationalOf(other))

operator fun RationalNumber.div(other: Int): RationalNumber =
    divide(rationalOf(other))

operator fun RationalNumber.rem(n: Int): RationalNumber =
    RationalNumber.valueOf(toBigDecimal() % BigDecimal.valueOf(n.toLong()))

operator fun RationalNumber.plus(other: Long): RationalNumber =
    add(rationalOf(other))

operator fun RationalNumber.minus(other: Long): RationalNumber =
    subtract(rationalOf(other))

operator fun RationalNumber.times(other: Long): RationalNumber =
    multiply(rationalOf(other))

operator fun RationalNumber.div(other: Long): RationalNumber =
    divide(rationalOf(other))

operator fun RationalNumber.rem(n: Long): RationalNumber =
    RationalNumber.valueOf(toBigDecimal() % BigDecimal.valueOf(n))

operator fun Int.plus(other: RationalNumber) = rationalOf(this) + other
operator fun Int.minus(other: RationalNumber) = rationalOf(this) - other
operator fun Int.times(other: RationalNumber) = rationalOf(this) * other
operator fun Int.div(other: RationalNumber) = rationalOf(this) / other

operator fun Long.plus(other: RationalNumber) = rationalOf(this) + other
operator fun Long.minus(other: RationalNumber) = rationalOf(this) - other
operator fun Long.times(other: RationalNumber) = rationalOf(this) * other
operator fun Long.div(other: RationalNumber) = rationalOf(this) / other

operator fun RationalNumber.unaryMinus(): RationalNumber = negate()

fun RationalNumber.aboutZero() = -EPSILON <= this && this <= EPSILON

fun RationalNumber.ceil() = if (rem(1) == RationalNumber.ZERO) this else floor() + 1
fun RationalNumber.floor() = if (rem(1) == RationalNumber.ZERO) this else subtract(rem(1))
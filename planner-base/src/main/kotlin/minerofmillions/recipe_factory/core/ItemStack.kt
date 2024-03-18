package minerofmillions.recipe_factory.core

import minerofmillions.utils.*
import org.ojalgo.scalar.RationalNumber

class ItemStack(val item: String, val amount: RationalNumber) : Comparable<ItemStack> {
    constructor(item: String, amount: Int) : this(item, rationalOf(amount))
    constructor(item: String, amount: Long) : this(item, rationalOf(amount))

    override fun toString(): String = buildString {
        append("%.3f".format(amount.toBigDecimal()).dropLastWhile('0'::equals).dropLastWhile('.'::equals))
        append(' ')
        append(item)
    }

    operator fun times(n: Int) = ItemStack(item, amount * n)
    operator fun times(n: Long) = ItemStack(item, amount * n)
    operator fun times(n: RationalNumber) = ItemStack(item, amount * n)

    operator fun div(n: Int) = ItemStack(item, amount / n)
    operator fun div(n: Long) = ItemStack(item, amount / n)
    operator fun div(n: RationalNumber) = ItemStack(item, amount / n)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemStack

        if (item != other.item) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = item.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun compareTo(other: ItemStack): Int = comparator.compare(this, other)

    companion object {
        private val comparator = compareBy(ItemStack::item).thenBy(ItemStack::amount)
    }
}

fun Collection<ItemStack>.mergeStacks() = groupBy { it.item }.mapNotNull { (item, stacks) ->
    ItemStack(item, stacks.sumOf(ItemStack::amount)).takeIf { !it.amount.aboutZero() }
}

fun MutableCollection<ItemStack>.addStack(stack: ItemStack): Boolean =
    firstOrNull { it.item == stack.item }?.let {
        remove(it);
        add(ItemStack(it.item, it.amount + stack.amount))
    } ?: add(stack)

fun MutableCollection<ItemStack>.addAllStacks(stacks: Iterable<ItemStack>) = stacks.forEach(::addStack)

operator fun String.times(n: Int) = ItemStack(this, n)
operator fun String.times(n: Long) = ItemStack(this, n)
operator fun String.times(n: RationalNumber) = ItemStack(this, n)

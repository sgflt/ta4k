/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Support or resistance at the price where the market turned most often in the last [lookback] bars.
 *
 * A turn is a change of direction between consecutive different values; its price is the value at the turn.
 * Turns within `bucketSize` of a bucket's first turn are counted together.
 */
abstract class AbstractBounceCountIndicator(
    price: NumericIndicator,
    lookback: Int,
    bucketSize: Number,
    preferLowerPrice: Boolean,
) : AbstractPriceLevelIndicator(price, lookback, price.numFactory.numOf(bucketSize), preferLowerPrice) {

    // one extra value to know the direction into the oldest bar
    private val window = ArrayDeque<Pair<Num, Long>>(lookback + 1)

    init {
        require(lookback > 1) { "Lookback must be greater than 1, but was: $lookback" }
        require(!tolerance.isNegative) { "Bucket size must not be negative, but was: $bucketSize" }
    }

    override val anchorOnFirstPoint = true

    /** True for turns this level type counts, given the direction before and after (+1 up, -1 down). */
    protected abstract fun isBounce(previousDirection: Int, newDirection: Int): Boolean

    override fun onWindowBar(bar: Bar, sequence: Long) {
        if (window.size == lookback + 1) window.removeFirst()
        window.addLast(price.value to sequence)
    }

    override fun points(): List<Point> {
        val one = numFactory.one()
        val firstInWindow = window.last().second - lookback + 1
        val bounces = ArrayList<Point>()
        var previous: Pair<Num, Long>? = null
        var previousDirection = 0
        for (sample in window) {
            val (value, _) = sample
            if (value.isNaN) continue
            val last = previous
            previous = sample
            if (last == null) continue
            val delta = value.compareTo(last.first)
            if (delta == 0) continue
            val direction = if (delta > 0) 1 else -1
            if (previousDirection != 0 && direction != previousDirection && last.second >= firstInWindow &&
                isBounce(previousDirection, direction)
            ) {
                bounces += Point(last.first, one, last.second)
            }
            previousDirection = direction
        }
        return bounces
    }
}

/** Support where the price turned up most often (troughs). */
class BounceCountSupportIndicator(price: NumericIndicator, lookback: Int, bucketSize: Number) :
    AbstractBounceCountIndicator(price, lookback, bucketSize, preferLowerPrice = true) {
    override fun isBounce(previousDirection: Int, newDirection: Int) = previousDirection < 0 && newDirection > 0
    override fun toString() = "BounceCountSupport($lookback) => $value"
}

/** Resistance where the price turned down most often (peaks). */
class BounceCountResistanceIndicator(price: NumericIndicator, lookback: Int, bucketSize: Number) :
    AbstractBounceCountIndicator(price, lookback, bucketSize, preferLowerPrice = false) {
    override fun isBounce(previousDirection: Int, newDirection: Int) = previousDirection > 0 && newDirection < 0
    override fun toString() = "BounceCountResistance($lookback) => $value"
}

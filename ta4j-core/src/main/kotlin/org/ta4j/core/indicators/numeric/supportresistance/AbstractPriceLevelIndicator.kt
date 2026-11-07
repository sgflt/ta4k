/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Base for support and resistance levels found in the last [lookback] bars.
 *
 * Subclasses turn the window into weighted price points; the points are grouped into levels of width
 * [tolerance] and the value is the weighted mean price of the strongest level. Ties go to the higher total weight,
 * then more points, then the lower price for support or the higher price for resistance, then the more recent
 * level. NaN while no level exists.
 */
abstract class AbstractPriceLevelIndicator(
    protected val price: NumericIndicator,
    protected val lookback: Int,
    protected val tolerance: Num,
    private val preferLowerPrice: Boolean,
) : NumericIndicator(price.numFactory) {

    /** A price point inside the window; [sequence] grows with time. */
    protected data class Point(val price: Num, val weight: Num, val sequence: Long)

    private class Level(first: Point, val anchor: Num) {
        var weightedSum: Num = first.price * first.weight
        var totalWeight: Num = first.weight
        var count = 1
        var lastSequence = first.sequence
        val price: Num get() = weightedSum / totalWeight

        fun add(point: Point) {
            weightedSum += point.price * point.weight
            totalWeight += point.weight
            ++count
            lastSequence = point.sequence
        }
    }

    private var sequence = 0L

    /** Called once per bar before [points]; subclasses update their window here. */
    protected abstract fun onWindowBar(bar: Bar, sequence: Long)

    /** Weighted price points of the current window, oldest first. */
    protected abstract fun points(): List<Point>

    /** True to measure the distance to a level from its first point, false from its running mean. */
    protected open val anchorOnFirstPoint = false

    private fun strongestLevel(): Level? {
        val levels = ArrayList<Level>()
        for (point in points()) {
            val level = levels.firstOrNull { level ->
                val reference = if (anchorOnFirstPoint) level.anchor else level.price
                (point.price - reference).abs() <= tolerance
            }
            if (level == null) levels.add(Level(point, point.price)) else level.add(point)
        }
        return levels.reduceOrNull { best, level -> if (isStronger(level, best)) level else best }
    }

    private fun isStronger(level: Level, best: Level): Boolean {
        val byWeight = level.totalWeight.compareTo(best.totalWeight)
        if (byWeight != 0) return byWeight > 0
        if (level.count != best.count) return level.count > best.count
        val byPrice = level.price.compareTo(best.price)
        if (byPrice != 0) return if (preferLowerPrice) byPrice < 0 else byPrice > 0
        return level.lastSequence > best.lastSequence
    }

    override fun updateState(bar: Bar) {
        price.onBar(bar)
        onWindowBar(bar, sequence++)
        value = strongestLevel()?.price ?: NaN
    }

    override val lag: Int
        get() = lookback

    override val isStable: Boolean
        get() = sequence >= lookback && price.isStable
}

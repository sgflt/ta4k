/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool.fractal

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Bill Williams fractal confirmation.
 *
 * The candidate is the bar [followingBars] bars ago. It is a fractal when its price strictly dominates the
 * [precedingBars] bars before it and the [followingBars] bars after it. To avoid look-ahead the indicator is true on
 * the confirmation bar (the current one), not on the pivot; [pivotPrice] is the price of the confirmed pivot.
 */
abstract class AbstractFractalIndicator(
    private val price: NumericIndicator,
    private val precedingBars: Int,
    /** Bars between the pivot and the bar that confirms it. */
    val followingBars: Int,
) : BooleanIndicator() {

    private val window = ArrayDeque<Num>(precedingBars + followingBars + 1)

    /** Number factory of the watched price. */
    val numFactory: NumFactory
        get() = price.numFactory

    /** Price of the pivot confirmed on this bar, NaN when there is none. */
    var pivotPrice: Num = NaN
        private set

    init {
        require(precedingBars >= 1) { "Preceding bars must be positive, but was: $precedingBars" }
        require(followingBars >= 1) { "Following bars must be positive, but was: $followingBars" }
    }

    /** True when [candidate] strictly dominates [other] in this fractal's direction. */
    protected abstract fun dominates(candidate: Num, other: Num): Boolean

    override fun updateState(bar: Bar) {
        price.onBar(bar)
        if (window.size == precedingBars + followingBars + 1) window.removeFirst()
        window.addLast(price.value)

        val candidate = if (isStable) window[precedingBars] else NaN
        value = !candidate.isNaN && window.withIndex().all { (i, other) ->
            i == precedingBars || (!other.isNaN && dominates(candidate, other))
        }
        pivotPrice = if (value) candidate else NaN
    }

    override val lag = precedingBars + followingBars + 1

    override val isStable: Boolean
        get() = window.size == precedingBars + followingBars + 1 && price.isStable
}

/** Fractal high: the pivot high is greater than the highs around it. */
class FractalHighIndicator(highPrice: NumericIndicator, precedingBars: Int = 2, followingBars: Int = 2) :
    AbstractFractalIndicator(highPrice, precedingBars, followingBars) {
    override fun dominates(candidate: Num, other: Num) = candidate > other
    override fun toString() = "FractalHigh => $value"
}

/** Fractal low: the pivot low is lower than the lows around it. */
class FractalLowIndicator(lowPrice: NumericIndicator, precedingBars: Int = 2, followingBars: Int = 2) :
    AbstractFractalIndicator(lowPrice, precedingBars, followingBars) {
    override fun dominates(candidate: Num, other: Num) = candidate < other
    override fun toString() = "FractalLow => $value"
}

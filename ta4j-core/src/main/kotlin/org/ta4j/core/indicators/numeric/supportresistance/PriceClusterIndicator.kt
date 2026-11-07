/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * Support or resistance at the price where most weight (volume by default) traded in the last [lookback] bars.
 *
 * Each bar adds its price with its weight to the first cluster whose weighted mean is within `tolerance`, or starts
 * a new cluster. Bars with a NaN price or a weight that is not positive are skipped.
 */
abstract class AbstractPriceClusterIndicator(
    price: NumericIndicator,
    private val weight: NumericIndicator,
    lookback: Int,
    tolerance: Number,
    preferLowerPrice: Boolean,
) : AbstractPriceLevelIndicator(price, lookback, price.numFactory.numOf(tolerance), preferLowerPrice) {

    private val window = ArrayDeque<Point>(lookback)

    init {
        require(lookback > 0) { "Lookback must be positive, but was: $lookback" }
        require(!this.tolerance.isNegative) { "Tolerance must not be negative, but was: $tolerance" }
    }

    override fun onWindowBar(bar: Bar, sequence: Long) {
        weight.onBar(bar)
        while (window.isNotEmpty() && window.first().sequence <= sequence - lookback) window.removeFirst()
        val barWeight = weight.value
        if (!price.value.isNaN && !barWeight.isNaN && barWeight.isPositive) {
            window.addLast(Point(price.value, barWeight, sequence))
        }
    }

    override fun points(): List<Point> = window
}

/** Support at the heaviest price cluster; ties prefer the lower price. */
class PriceClusterSupportIndicator(price: NumericIndicator, weight: NumericIndicator, lookback: Int, tolerance: Number) :
    AbstractPriceClusterIndicator(price, weight, lookback, tolerance, preferLowerPrice = true) {
    override fun toString() = "PriceClusterSupport($lookback) => $value"
}

/** Resistance at the heaviest price cluster; ties prefer the higher price. */
class PriceClusterResistanceIndicator(
    price: NumericIndicator,
    weight: NumericIndicator,
    lookback: Int,
    tolerance: Number,
) : AbstractPriceClusterIndicator(price, weight, lookback, tolerance, preferLowerPrice = false) {
    override fun toString() = "PriceClusterResistance($lookback) => $value"
}

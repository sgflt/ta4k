/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.statistics.ZScoreIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Base for volume-weighted average price indicators.
 *
 * Keeps running sums of `price * volume`, `price² * volume` and `volume` over a window that subclasses manage
 * through [add], [remove] and [reset]. The value is the weighted mean; [variance] and [standardDeviation] are the
 * volume-weighted dispersion of the price around it.
 *
 * Bars with a NaN price or volume, or a negative volume, add nothing. While the window has no volume the value is
 * the current price.
 */
abstract class AbstractVWAPIndicator(
    protected val price: NumericIndicator,
    protected val volume: NumericIndicator,
) : NumericIndicator(price.numFactory) {

    private var weightedSum = numFactory.zero()
    private var weightedSquareSum = numFactory.zero()
    private var volumeSum = numFactory.zero()

    /** Volume-weighted variance of the price in the current window. */
    var variance: Num = NaN
        protected set

    /** Volume-weighted standard deviation of the price in the current window. */
    val standardDeviation: Num
        get() = if (variance.isNaN) NaN else variance.sqrt()

    /** Moves the window for the new bar: subclasses call [add], [remove] or [reset]. */
    protected abstract fun updateWindow(bar: Bar, price: Num, volume: Num)

    protected fun isValid(price: Num, volume: Num) = !price.isNaN && !volume.isNaN && !volume.isNegative

    protected fun add(price: Num, volume: Num) {
        if (!isValid(price, volume)) return
        weightedSum += price * volume
        weightedSquareSum += price * price * volume
        volumeSum += volume
    }

    protected fun remove(price: Num, volume: Num) {
        if (!isValid(price, volume)) return
        weightedSum -= price * volume
        weightedSquareSum -= price * price * volume
        volumeSum -= volume
    }

    protected fun reset() {
        weightedSum = numFactory.zero()
        weightedSquareSum = numFactory.zero()
        volumeSum = numFactory.zero()
    }

    override fun updateState(bar: Bar) {
        price.onBar(bar)
        volume.onBar(bar)
        updateWindow(bar, price.value, volume.value)

        if (volumeSum.isNegativeOrZero) {
            value = price.value
            variance = if (price.value.isNaN) NaN else numFactory.zero()
            return
        }
        val mean = weightedSum / volumeSum
        value = mean
        // rolling sums can drift slightly below zero when the window is flat
        val rawVariance = weightedSquareSum / volumeSum - mean * mean
        variance = if (rawVariance.isNegative) numFactory.zero() else rawVariance
    }

    /** Volume-weighted standard deviation of the price around this VWAP. */
    fun standardDeviation() = VWAPStandardDeviationIndicator(this)

    /** `VWAP + multiplier * standard deviation`. */
    fun upperBand(multiplier: Number) = plus(standardDeviation().multipliedBy(multiplier))

    /** `VWAP - multiplier * standard deviation`. */
    fun lowerBand(multiplier: Number) = minus(standardDeviation().multipliedBy(multiplier))

    /** Distance of [price] (the VWAP's own price by default) from the VWAP. */
    fun deviation(price: NumericIndicator = this.price) = price.minus(this)

    /** Deviation from the VWAP in units of its standard deviation. */
    fun zScore(price: NumericIndicator = this.price) = ZScoreIndicator(deviation(price), standardDeviation())
}

/**
 * Volume-weighted standard deviation of the price around a VWAP.
 */
class VWAPStandardDeviationIndicator(private val vwap: AbstractVWAPIndicator) : NumericIndicator(vwap.numFactory) {

    override fun updateState(bar: Bar) {
        vwap.onBar(bar)
        value = vwap.standardDeviation
    }

    override val lag: Int
        get() = vwap.lag

    override val isStable: Boolean
        get() = vwap.isStable

    override fun toString() = "VWAPStdDev($vwap) => $value"
}

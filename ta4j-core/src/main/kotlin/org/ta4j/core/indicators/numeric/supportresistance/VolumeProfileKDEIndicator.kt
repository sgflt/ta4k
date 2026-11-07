/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.supportresistance

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Volume-at-price profile of the last [lookback] bars, smoothed with a Gaussian kernel density estimate.
 *
 * The value is the density at the current price, i.e. how much volume traded near it. [densityAt] evaluates any
 * price and [modePrice] is the most liquid price of the window (a volume point of control). A [bandwidth] of zero
 * counts only volume at exactly the same price.
 *
 * @see <a href="https://www.investopedia.com/terms/v/volume-profile.asp">Investopedia: Volume Profile</a>
 */
class VolumeProfileKDEIndicator(
    private val price: NumericIndicator,
    private val volume: NumericIndicator,
    private val lookback: Int,
    bandwidth: Number,
) : NumericIndicator(price.numFactory) {

    private val bandwidthValue = bandwidth.toDouble()
    private val window = ArrayDeque<Pair<Num, Num>>(lookback)
    private var barsSeen = 0

    init {
        require(lookback > 0) { "Lookback must be positive, but was: $lookback" }
        require(bandwidthValue >= 0 && bandwidthValue.isFinite()) { "Bandwidth must not be negative, was: $bandwidth" }
    }

    private fun samples() = window.filter { (p, _) -> !p.isNaN }

    /** Volume density at [atPrice] over the current window, NaN while the window has no volume. */
    fun densityAt(atPrice: Num): Num {
        val samples = samples()
        if (atPrice.isNaN || samples.isEmpty()) return NaN
        if (bandwidthValue == 0.0) {
            return samples.filter { (p, _) -> p.compareTo(atPrice) == 0 }
                .fold(numFactory.zero()) { sum, (_, w) -> sum + w }
        }
        val coefficient = 1.0 / (bandwidthValue * sqrt(2 * PI))
        var density = 0.0
        for ((p, w) in samples) {
            val z = (atPrice.doubleValue() - p.doubleValue()) / bandwidthValue
            density += w.doubleValue() * coefficient * exp(-0.5 * z * z)
        }
        return numFactory.numOf(density)
    }

    /** Window price with the highest density; ties go to the lower price. NaN while the window has no volume. */
    val modePrice: Num
        get() {
            var best: Num = NaN
            var bestDensity: Num = NaN
            for ((p, _) in samples()) {
                val density = densityAt(p)
                if (bestDensity.isNaN || density > bestDensity || (density.compareTo(bestDensity) == 0 && p < best)) {
                    best = p
                    bestDensity = density
                }
            }
            return best
        }

    override fun updateState(bar: Bar) {
        price.onBar(bar)
        volume.onBar(bar)
        ++barsSeen
        // every bar takes a slot so the window spans lookback bars; bars without volume carry zero weight
        if (window.size == lookback) window.removeFirst()
        val barVolume = volume.value
        val usable = !price.value.isNaN && !barVolume.isNaN && !barVolume.isZero
        window.addLast(if (usable) price.value to barVolume.abs() else NaN to numFactory.zero())
        value = densityAt(price.value)
    }

    override val lag = lookback

    override val isStable: Boolean
        get() = barsSeen >= lookback && price.isStable && volume.isStable

    override fun toString() = "VolumeProfileKDE($lookback) => $value"
}

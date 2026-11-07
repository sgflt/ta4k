/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volume

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Klinger volume force: `volume * trend * |2 * (dm / cm - 1)| * scale`.
 *
 * - `dm` is the bar range (high - low)
 * - `trend` is +1 when high + low + close rose, -1 when it fell, unchanged otherwise
 * - `cm` adds `dm` up while the trend holds and restarts from the previous and current `dm` when it flips
 *
 * The first bar has no trend yet and gives NaN. Zero volume or a zero `cm` gives zero force.
 */
class KlingerVolumeForceIndicator(
    private val highPrice: NumericIndicator,
    private val lowPrice: NumericIndicator,
    private val closePrice: NumericIndicator,
    private val volume: NumericIndicator,
    scaleMultiplier: Number = 100,
) : NumericIndicator(highPrice.numFactory) {

    private val scale = numFactory.numOf(scaleMultiplier)
    private var previousBasis: Num = NaN
    private var previousMeasurement: Num = NaN
    private var trend = 1
    private var cumulativeMeasurement: Num = NaN
    private var barsPassed = 0

    override fun updateState(bar: Bar) {
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        closePrice.onBar(bar)
        volume.onBar(bar)
        ++barsPassed

        val measurement = highPrice.value - lowPrice.value
        val basis = highPrice.value + lowPrice.value + closePrice.value
        if (previousBasis.isNaN) {
            cumulativeMeasurement = measurement
            value = NaN
        } else {
            val previousTrend = trend
            if (basis > previousBasis) trend = 1 else if (basis < previousBasis) trend = -1
            cumulativeMeasurement = if (trend == previousTrend) {
                cumulativeMeasurement + measurement
            } else {
                previousMeasurement + measurement
            }
            value = force(measurement)
        }
        previousBasis = basis
        previousMeasurement = measurement
    }

    private fun force(measurement: Num): Num {
        val barVolume = volume.value
        if (barVolume.isZero || cumulativeMeasurement.isZero) {
            return numFactory.zero()
        }
        val magnitude = (numFactory.two() * (measurement / cumulativeMeasurement - numFactory.one())).abs()
        return barVolume * numFactory.numOf(trend) * magnitude * scale
    }

    override val lag = 2

    override val isStable: Boolean
        get() = barsPassed >= 2

    override fun toString() = "KlingerVolumeForce => $value"
}

/**
 * Klinger Volume Oscillator: `EMA(volume force, shortPeriod) - EMA(volume force, longPeriod)`.
 *
 * @see <a href="https://www.investopedia.com/terms/k/klingeroscillator.asp">Investopedia</a>
 */
class KlingerVolumeOscillatorIndicator(
    volumeForce: NumericIndicator,
    private val shortPeriod: Int = 34,
    private val longPeriod: Int = 55,
) : NumericIndicator(volumeForce.numFactory) {

    init {
        require(shortPeriod > 0) { "Short period must be positive, but was: $shortPeriod" }
        require(longPeriod > shortPeriod) { "Long period must exceed short period, but was: $longPeriod" }
    }

    private val shortEma = volumeForce.ema(shortPeriod)
    private val longEma = volumeForce.ema(longPeriod)

    override fun updateState(bar: Bar) {
        shortEma.onBar(bar)
        longEma.onBar(bar)
        value = shortEma.value - longEma.value
    }

    override val lag = longPeriod + 1

    override val isStable: Boolean
        get() = shortEma.isStable && longEma.isStable

    override fun toString() = "KVO($shortPeriod, $longPeriod) => $value"
}

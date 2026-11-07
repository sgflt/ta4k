/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.volatility

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.TRIndicator
import org.ta4j.core.indicators.numeric.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory

/**
 * TTM Squeeze Pro (LazyBear / TradingView parity).
 *
 * The value is the squeeze momentum histogram: a linear regression over [barCount] bars of
 * `close - avg(avg(highest high, lowest low), sma(close))`.
 *
 * [squeezeLevel] tells how tightly the Bollinger Bands (SMA ± [bollingerK] σ) sit inside Keltner Channels of
 * increasing width (SMA ± factor * SMA(true range)): HIGH for the narrowest channel, then MID and LOW.
 */
class SqueezeProIndicator(
    numFactory: NumFactory,
    private val barCount: Int = 20,
    bollingerK: Number = 2,
    keltnerHigh: Number = 1,
    keltnerMid: Number = 1.5,
    keltnerLow: Number = 2,
) : NumericIndicator(numFactory) {

    enum class SqueezeLevel { NONE, LOW, MID, HIGH }

    private val factory = Indicators.extended(numFactory)
    private val close = factory.closePrice()
    private val basis = close.sma(barCount)
    private val stdDev = close.stddev(barCount)
    private val trueRangeAverage = TRIndicator(numFactory).sma(barCount)
    private val momentum = SimpleLinearRegressionIndicator(
        close.minus(
            factory.highPrice().highest(barCount).plus(factory.lowPrice().lowest(barCount)).dividedBy(2)
                .plus(basis).dividedBy(2)
        ),
        barCount,
    )
    private val k = numFactory.numOf(bollingerK)
    private val levels = listOf(
        SqueezeLevel.HIGH to numFactory.numOf(keltnerHigh),
        SqueezeLevel.MID to numFactory.numOf(keltnerMid),
        SqueezeLevel.LOW to numFactory.numOf(keltnerLow),
    )

    /** Compression level on the current bar; NONE while unstable. */
    var squeezeLevel = SqueezeLevel.NONE
        private set

    init {
        require(barCount > 1) { "Bar count must be greater than 1, but was: $barCount" }
    }

    override fun updateState(bar: Bar) {
        basis.onBar(bar)
        stdDev.onBar(bar)
        trueRangeAverage.onBar(bar)
        momentum.onBar(bar)

        if (!isStable) {
            value = NaN
            squeezeLevel = SqueezeLevel.NONE
            return
        }
        value = momentum.value
        val bollingerWidth = stdDev.value * k
        squeezeLevel = levels.firstOrNull { (_, factor) -> bollingerWidth < trueRangeAverage.value * factor }?.first
            ?: SqueezeLevel.NONE
    }

    /** True while the Bollinger Bands are inside at least the widest Keltner Channel. */
    fun inSqueeze(): BooleanIndicator {
        val source = this
        return object : BooleanIndicator() {
            override fun updateState(bar: Bar) {
                source.onBar(bar)
                value = source.squeezeLevel != SqueezeLevel.NONE
            }

            override val lag: Int
                get() = source.lag

            override val isStable: Boolean
                get() = source.isStable

            override fun toString() = "InSqueeze => $value"
        }
    }

    override val lag = 2 * barCount

    override val isStable: Boolean
        get() = basis.isStable && stdDev.isStable && trueRangeAverage.isStable && momentum.isStable

    override fun toString() = "SqueezePro($barCount) => $value ($squeezeLevel)"
}

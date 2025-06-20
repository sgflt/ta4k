/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric.supertrend

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator
import org.ta4j.core.num.NumFactory

/**
 * SuperTrend Upper Band Indicator.
 *
 * Calculates the upper band for the SuperTrend indicator with smoothing logic.
 * The upper band is calculated as: (high + low) / 2 + multiplier * ATR
 *
 * Smoothing rule: Use the minimum of current upper band and previous upper band,
 * unless the previous close was above the previous upper band.
 */
private class SuperTrendUpperBandIndicator(
    numFactory: NumFactory,
    private val atr: ATRIndicator,
    private val multiplier: Double,
) : NumericIndicator(numFactory) {

    private val high = Indicators.extended(numFactory).highPrice()
    private val low = Indicators.extended(numFactory).lowPrice()
    private val close = Indicators.extended(numFactory).closePrice()

    private var previousBand = numFactory.zero()
    private var previousClose = numFactory.zero()
    private var isFirstBar = true

    override fun updateState(bar: Bar) {
        atr.onBar(bar)
        high.onBar(bar)
        low.onBar(bar)
        close.onBar(bar)

        val medianPrice = (high.value + low.value) / numFactory.two()
        val rawBand = medianPrice + numFactory.numOf(multiplier) * atr.value

        if (isFirstBar) {
            isFirstBar = false
            value = rawBand
            previousBand = rawBand
            previousClose = close.value
            return
        }

        // Apply smoothing: use current band if it's lower than previous, 
        // or if previous close broke above previous band
        value = if (rawBand < previousBand || previousClose > previousBand) {
            rawBand
        } else {
            previousBand
        }

        previousBand = value
        previousClose = close.value
    }

    override val lag = atr.lag

    override val isStable: Boolean
        get() = atr.isStable && !isFirstBar

    override fun toString(): String {
        return "SuperTrendUpperBand($multiplier) => $value"
    }
}

/**
 * SuperTrend Lower Band Indicator.
 *
 * Calculates the lower band for the SuperTrend indicator with smoothing logic.
 * The lower band is calculated as: (high + low) / 2 - multiplier * ATR
 *
 * Smoothing rule: Use the maximum of current lower band and previous lower band,
 * unless the previous close was below the previous lower band.
 */
private class SuperTrendLowerBandIndicator(
    numFactory: NumFactory,
    private val atr: ATRIndicator,
    private val multiplier: Double,
) : NumericIndicator(numFactory) {

    private val high = Indicators.extended(numFactory).highPrice()
    private val low = Indicators.extended(numFactory).lowPrice()
    private val close = Indicators.extended(numFactory).closePrice()

    private var previousBand = numFactory.zero()
    private var previousClose = numFactory.zero()
    private var isFirstBar = true

    override fun updateState(bar: Bar) {
        atr.onBar(bar)
        high.onBar(bar)
        low.onBar(bar)
        close.onBar(bar)

        val medianPrice = (high.value + low.value) / numFactory.two()
        val rawBand = medianPrice - numFactory.numOf(multiplier) * atr.value

        if (isFirstBar) {
            isFirstBar = false
            value = rawBand
            previousBand = rawBand
            previousClose = close.value
            return
        }

        // Apply smoothing: use current band if it's higher than previous,
        // or if previous close broke below previous band
        value = if (rawBand > previousBand || previousClose < previousBand) {
            rawBand
        } else {
            previousBand
        }

        previousBand = value
        previousClose = close.value
    }

    override val lag = atr.lag

    override val isStable: Boolean
        get() = atr.isStable && !isFirstBar

    override fun toString(): String {
        return "SuperTrendLowerBand($multiplier) => $value"
    }
}

/**
 * The SuperTrend indicator.
 *
 * SuperTrend is a trend-following indicator that uses Average True Range (ATR)
 * to calculate dynamic support and resistance levels. The indicator switches
 * between upper and lower bands based on price action:
 *
 * - When trend is up: SuperTrend = Lower Band
 * - When trend is down: SuperTrend = Upper Band
 *
 * The trend changes when price crosses from one side of the band to the other.
 *
 * @param numFactory the number factory for calculations
 * @param barCount the time frame for ATR calculation (default: 10)
 * @param multiplier the ATR multiplier for band calculation (default: 3.0)
 *
 * @see <a href="https://zerodha.com/varsity/chapter/supertrend/">SuperTrend Explanation</a>
 */
class SuperTrendIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val barCount: Int = 10,
    private val multiplier: Double = 3.0,
) : NumericIndicator(numFactory) {

    private val atr = Indicators.extended(numFactory).atr(barCount)
    private val superTrendUpperBandIndicator = SuperTrendUpperBandIndicator(numFactory, atr, multiplier)
    private val superTrendLowerBandIndicator = SuperTrendLowerBandIndicator(numFactory, atr, multiplier)
    private val close = Indicators.extended(numFactory).closePrice()

    private var previousSuperTrend = numFactory.zero()
    private var previousUpperBand = numFactory.zero()
    private var isFirstBar = true

    override fun updateState(bar: Bar) {
        superTrendUpperBandIndicator.onBar(bar)
        superTrendLowerBandIndicator.onBar(bar)
        close.onBar(bar)

        val upperBand = superTrendUpperBandIndicator.value
        val lowerBand = superTrendLowerBandIndicator.value

        if (isFirstBar) {
            isFirstBar = false
            // Start in uptrend (SuperTrend = lower band)
            value = lowerBand
            previousSuperTrend = value
            previousUpperBand = upperBand
            return
        }

        // Determine SuperTrend value based on previous trend direction
        value = if (previousSuperTrend == previousUpperBand) {
            // Previous trend was down (SuperTrend was upper band)
            if (close.value <= upperBand) {
                upperBand  // Continue downtrend
            } else {
                lowerBand  // Switch to uptrend
            }
        } else {
            // Previous trend was up (SuperTrend was lower band)  
            if (close.value >= lowerBand) {
                lowerBand  // Continue uptrend
            } else {
                upperBand  // Switch to downtrend
            }
        }

        previousSuperTrend = value
        previousUpperBand = upperBand
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = superTrendUpperBandIndicator.isStable && superTrendLowerBandIndicator.isStable && !isFirstBar

    override fun toString(): String {
        return "SuperTrend($barCount, $multiplier) => $value"
    }
}

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
package org.ta4j.core.indicators.numeric.oscillators

import kotlin.math.ln
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The Fisher Transform Indicator.
 *
 * The Fisher Transform converts prices into a Gaussian normal distribution.
 * It normalizes price data to values between -1 and 1, then applies the Fisher transform
 * to make extreme price movements more apparent.
 *
 * Formula:
 * 1. value = 0.33 * 2 * ((price - lowestLow) / (highestHigh - lowestLow) - 0.5) + 0.67 * previous value
 * 2. value = clamp(value, -0.9999, 0.9999)
 * 3. fisher = 0.5 * ln((1 + value) / (1 - value)) + 0.5 * previous fisher
 *
 * @param numFactory the number factory for calculations
 * @param price the price indicator (typically median price)
 * @param barCount the lookback period for highest/lowest (default: 10)
 *
 * @see <a href="https://www.investopedia.com/terms/f/fisher-transform.asp">
 *      Fisher Transform - Investopedia</a>
 */
class FisherIndicator(
    numFactory: NumFactory,
    private val barCount: Int = 10,
) : NumericIndicator(numFactory) {
    private val price = Indicators.extended(numFactory).medianPrice()
    private val highest = Indicators.extended(numFactory).highPrice().highest(barCount)
    private val lowest = Indicators.extended(numFactory).lowPrice().lowest(barCount)

    private val alpha = numFactory.numOf(0.33)
    private val beta = numFactory.numOf(0.67)
    private val half = numFactory.numOf(0.5)
    private val two = numFactory.two()
    private val maxValue = numFactory.numOf(0.9999)
    private val minValue = numFactory.numOf(-0.9999)

    private var previousValue = numFactory.zero()
    private var previousFisher = numFactory.zero()

    init {
        require(barCount > 0) { "Bar count must be positive" }
    }

    private fun calculate(): Num {
        val range = highest.value - lowest.value

        // Step 1: Normalize price to [-1, 1] and smooth
        val normalizedValue = if (range.isZero) {
            numFactory.zero()
        } else {
            two * ((price.value - lowest.value) / range) - numFactory.one()
        }

        val smoothedValue = alpha * normalizedValue + beta * previousValue

        // Step 2: Clamp value to avoid infinity
        val clampedValue = when {
            smoothedValue > maxValue -> maxValue
            smoothedValue < minValue -> minValue
            else -> smoothedValue
        }

        // Step 3: Apply Fisher transform with smoothing
        val fisherValue = try {
            val ratio = (numFactory.one() + clampedValue) / (numFactory.one() - clampedValue)
            half * numFactory.numOf(ln(ratio.doubleValue())) + half * previousFisher
        } catch (_: Exception) {
            previousFisher
        }

        previousValue = clampedValue
        previousFisher = fisherValue
        return fisherValue
    }

    override fun updateState(bar: Bar) {
        price.onBar(bar)
        highest.onBar(bar)
        lowest.onBar(bar)

        value = calculate()
    }

    override val lag = barCount

    override val isStable: Boolean
        get() = highest.isStable && lowest.isStable

    override fun toString(): String {
        return "Fisher($barCount) => $value"
    }
}

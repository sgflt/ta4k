/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric.average

import kotlin.math.max
import kotlin.math.min
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Jurik Moving Average (JMA) Indicator.
 *
 * JMA, or Jurik Moving Average, is a type of moving average developed by Mark
 * Jurik. It is known for its ability to respond to price changes more smoothly
 * than traditional moving averages like SMA (Simple Moving Average) or EMA
 * (Exponential Moving Average), while avoiding much of the lag associated with
 * those averages.
 *
 * @property indicator The input indicator (e.g., Close Price)
 * @property barCount The number of periods for the JMA
 * @property phase The phase adjustment (-100 to +100)
 * @property power The smoothing power factor (default is 2)
 */
class JMAIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
    phase: Double,
    power: Double,
) : NumericIndicator(numFactory) {

    private val phase = numFactory.numOf(min(max(phase, -100.0), 100.0)) // Clamp phase between -100 and 100
    private val power = numFactory.numOf(max(1.0, power)) // Ensure power is at least 1

    // Compute smoothing factor based on phase
    private val beta: Num = numFactory.numOf(0.45 * (barCount - 1)) / (numFactory.numOf(0.45 * (barCount - 1) + 2))
    private val phaseRatio: Num = when {
        this.phase < numFactory.numOf(-100) -> numFactory.numOf(0.5)
        this.phase > numFactory.hundred() -> numFactory.numOf(2.5)
        else -> this.phase / numFactory.hundred() + numFactory.numOf(1.5)
    }
    private val alpha: Num = beta.pow(this.power)

    // Previous JMA state
    private var previousE0: Num = numFactory.zero()
    private var previousE1: Num = numFactory.zero()
    private var previousE2: Num = numFactory.zero()
    private var previousJMA: Num = numFactory.zero()

    private var isFirstBar = true
    private var processedBars = 0

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        processedBars++

        val currentPrice = indicator.value

        if (isFirstBar) {
            previousE0 = currentPrice
            previousE1 = numFactory.zero()
            previousE2 = numFactory.zero()
            previousJMA = currentPrice
            value = currentPrice
            isFirstBar = false
            return
        }

        val e0 = calculateE0(currentPrice)
        val e1 = calculateE1(currentPrice, e0)
        val e2 = calculateE2(e0, e1)

        val jma = previousJMA.plus(e2)

        // Update previous values for next iteration
        previousE0 = e0
        previousE1 = e1
        previousE2 = e2
        previousJMA = jma

        value = jma
    }

    private fun calculateE0(currentPrice: Num): Num =
        currentPrice * (numFactory.one() - alpha) + (previousE0 * alpha)

    private fun calculateE1(currentPrice: Num, e0: Num): Num =
        (currentPrice - e0) * (numFactory.one() - beta) + (previousE1 * beta)

    private fun calculateE2(e0: Num, e1: Num): Num =
        (e0 + phaseRatio * e1 - previousJMA) * (numFactory.one() - alpha).pow(2) +
                (previousE2 * alpha.pow(2))

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars >= barCount

    override fun toString(): String {
        return "JMA(barCount: $barCount, phase: $phase, power: $power) => $value"
    }
}

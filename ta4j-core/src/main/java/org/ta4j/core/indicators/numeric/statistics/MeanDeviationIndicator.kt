/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.indicators.numeric.statistics

import java.util.*
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Mean deviation indicator.
 *
 * @see [
 * http://en.wikipedia.org/wiki/Mean_absolute_deviation.Average_absolute_deviation](http://en.wikipedia.org/wiki/Mean_absolute_deviation.Average_absolute_deviation)
 */
class MeanDeviationIndicator(private val indicator: NumericIndicator, private val barCount: Int) : NumericIndicator(
    indicator.numFactory
) {
    private val window = LinkedList<Num>()
    private val numBarCount = numFactory.numOf(barCount)
    private val divisor = numFactory.numOf(barCount)
    private var currentBar = 0
    private var sum = numFactory.zero()
    private var deviationSum = numFactory.zero()


    private fun calculate(): Num {
        if (window.size == barCount) {
            stablePath()
        }

        return unstablePath()
    }


    private fun unstablePath(): Num {
        val newValue = indicator.value
        window.offer(newValue)
        val oldMean = if (window.size == 1) newValue else sum / numFactory.numOf(window.size - 1)
        sum += newValue
        val newMean = sum / numFactory.numOf(window.size)

        deviationSum += (newValue - newMean).abs()

        for (value in window) {
            if (value != newValue) {
                deviationSum += (value - newMean).abs() - (value - oldMean).abs()
            }
        }

        return deviationSum / numFactory.numOf(window.size)
    }


    private fun stablePath() {
        val oldestValue = window.poll()
        val oldMean = sum / divisor

        // Remove contribution of oldest value
        sum -= oldestValue
        deviationSum -= (oldestValue - oldMean).abs()

        // Adjust deviationSum for the change in mean
        val newMean = sum / numFactory.numOf(barCount - 1)
        for (value in window) {
            deviationSum += (value - newMean).abs() - (value - oldMean).abs()
        }
    }


    override fun toString() = "MDI($numBarCount) => $value"


    public override fun updateState(bar: Bar) {
        ++currentBar
        indicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = currentBar >= barCount

    override val lag: Int
        get() = barCount
}

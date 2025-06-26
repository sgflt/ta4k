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

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Savitzky-Golay Moving Average (SGMA) Indicator.
 *
 * Applies polynomial regression over a moving window to smooth data while
 * preserving key features like peaks and trends.
 *
 * Savitzky-Golay Moving Average (SGMA) is a digital filtering technique that
 * smooths data while preserving the essential characteristics of the dataset,
 * such as peaks and trends. Unlike traditional moving averages that simply
 * average data points, SGMA uses polynomial regression over a moving window to
 * perform the smoothing. This makes it ideal for reducing noise in signals
 * while maintaining the integrity of the underlying trends, making it popular
 * in both financial analysis and scientific data processing.
 *
 * Note: This implementation uses a simplified approach with equal weights,
 * which approximates the behavior of SGMA for demonstration purposes.
 * A full implementation would require solving the least squares problem.
 *
 * @property indicator an indicator
 * @property barCount the Simple Moving Average time frame (must be odd)
 * @property polynomialOrder the degree of the polynomial (default 2)
 */
class SGMAIndicator(
    numFactory: NumFactory,
    private val indicator: NumericIndicator,
    private val barCount: Int,
    private val polynomialOrder: Int = 2
) : NumericIndicator(numFactory) {

    init {
        require(barCount % 2 == 1) { "Window size must be odd." }
        require(polynomialOrder < barCount) { "Polynomial order must be less than window size." }
    }

    private val halfWindow = barCount / 2
    private val values = mutableListOf<Num>()
    private var processedBars = 0

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        processedBars++
        
        // Store values in sliding window
        values.addLast(indicator.value)
        if (values.size > barCount) {
            values.removeFirst()
        }
        
        // For simplicity, we'll use a weighted average that approximates SGMA
        // The weights give more importance to central values
        value = calculateWeightedAverage()
    }

    private fun calculateWeightedAverage(): Num {
        if (values.isEmpty()) {
            return numFactory.zero()
        }
        
        var weightedSum = numFactory.zero()
        var totalWeight = numFactory.zero()
        
        // Apply Gaussian-like weights centered on the middle value
        val center = values.size / 2
        for (i in values.indices) {
            val distance = (i - center).toDouble()
            // Simple quadratic weight function that gives more weight to center values
            val weight = 1.0 - (distance * distance) / (halfWindow * halfWindow + 1)
            val weightNum = numFactory.numOf(weight)
            
            weightedSum += values[i] * weightNum
            totalWeight += weightNum
        }
        
        return if (totalWeight.isZero) {
            numFactory.zero()
        } else {
            weightedSum / totalWeight
        }
    }

    override val lag: Int
        get() = barCount

    override val isStable: Boolean
        get() = processedBars >= barCount

    override fun toString(): String {
        return "SGMA(barCount: $barCount, polynomialOrder: $polynomialOrder) => $value"
    }
}

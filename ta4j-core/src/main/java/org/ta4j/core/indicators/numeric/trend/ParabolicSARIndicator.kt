/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric.trend

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Parabolic SAR indicator.
 *
 * The Parabolic SAR (Stop and Reverse) is a trend-following indicator that provides
 * potential reversal points. It uses an acceleration factor that increases as the
 * trend continues, making the SAR more sensitive to price changes over time.
 *
 * The indicator works by:
 * 1. Determining the trend direction based on price movement
 * 2. Setting initial SAR based on extreme points of first two bars
 * 3. Calculating new SAR using: SAR = Previous SAR + AF × (EP - Previous SAR)
 * 4. Adjusting acceleration factor when new extreme points are reached
 * 5. Reversing trend when price crosses the SAR line
 *
 * @param numFactory the number factory
 * @param accelerationStart the initial acceleration factor (default: 0.02)
 * @param maxAcceleration the maximum acceleration factor (default: 0.2)
 * @param accelerationIncrement the increment step for acceleration factor (default: 0.02)
 *
 * @see <a href="https://www.investopedia.com/trading/introduction-to-parabolic-sar/">
 *      Introduction to Parabolic SAR</a>
 * @see <a href="https://www.investopedia.com/terms/p/parabolicindicator.asp">
 *      Parabolic SAR</a>
 */
class ParabolicSARIndicator(
    numFactory: NumFactory,
    private val accelerationStart: Num = numFactory.numOf(0.02),
    private val maxAcceleration: Num = numFactory.numOf(0.2),
    private val accelerationIncrement: Num = numFactory.numOf(0.02),
) : NumericIndicator(numFactory) {

    private val highPrice = HighPriceIndicator(numFactory)
    private val lowPrice = LowPriceIndicator(numFactory)
    private val closePrice = ClosePriceIndicator(numFactory)

    private val previousHigh = highPrice.previous()
    private val previousLow = lowPrice.previous()
    private val previousClose = closePrice.previous()

    // State variables
    private var isUpTrend = false
    private var extremePoint = numFactory.zero()
    private var accelerationFactor = numFactory.zero()
    private var previousSAR = numFactory.zero()
    private var barCount = 0

    init {
        require(accelerationStart.isPositive) { "Acceleration start must be positive" }
        require(maxAcceleration > accelerationStart) { "Max acceleration must be greater than start acceleration" }
        require(accelerationIncrement.isPositive) { "Acceleration increment must be positive" }
    }

    private fun calculate(): Num {
        when (barCount) {
            1 -> {
                // First bar - initialize but no SAR calculation possible
                extremePoint = closePrice.value
                return NaN
            }

            2 -> {
                // Second bar - determine initial trend direction and SAR
                isUpTrend = closePrice.value > previousClose.value
                accelerationFactor = accelerationStart

                if (isUpTrend) {
                    // In uptrend, SAR starts at the lowest of the first two lows
                    previousSAR = minOf(previousLow.value, lowPrice.value)
                    extremePoint = maxOf(previousHigh.value, highPrice.value)
                } else {
                    // In downtrend, SAR starts at the highest of the first two highs
                    previousSAR = maxOf(previousHigh.value, highPrice.value)
                    extremePoint = minOf(previousLow.value, lowPrice.value)
                }

                return previousSAR
            }

            else -> {
                // Calculate new SAR using the formula: SAR = Previous SAR + AF × (EP - Previous SAR)
                var newSAR = previousSAR + accelerationFactor * (extremePoint - previousSAR)

                val currentHigh = highPrice.value
                val currentLow = lowPrice.value

                if (isUpTrend) {
                    // Check for trend reversal in uptrend
                    if (currentLow <= newSAR) {
                        // Trend reversal to downtrend
                        isUpTrend = false
                        newSAR = extremePoint  // SAR becomes the previous extreme point
                        extremePoint = currentLow  // New extreme is current low
                        accelerationFactor = accelerationStart
                    } else {
                        // Continue uptrend - check for new extreme point
                        if (currentHigh > extremePoint) {
                            extremePoint = currentHigh
                            accelerationFactor = minOf(
                                accelerationFactor + accelerationIncrement,
                                maxAcceleration
                            )
                        }

                        // SAR cannot exceed the lowest low of the previous two bars
                        val lowestOfLast2 = minOf(previousLow.value, currentLow)
                        if (newSAR > lowestOfLast2) {
                            newSAR = lowestOfLast2
                        }
                    }
                } else {
                    // Check for trend reversal in downtrend
                    if (currentHigh >= newSAR) {
                        // Trend reversal to uptrend
                        isUpTrend = true
                        newSAR = extremePoint  // SAR becomes the previous extreme point
                        extremePoint = currentHigh  // New extreme is current high
                        accelerationFactor = accelerationStart
                    } else {
                        // Continue downtrend - check for new extreme point
                        if (currentLow < extremePoint) {
                            extremePoint = currentLow
                            accelerationFactor = minOf(
                                accelerationFactor + accelerationIncrement,
                                maxAcceleration
                            )
                        }

                        // SAR cannot go below the highest high of the previous two bars
                        val highestOfLast2 = maxOf(previousHigh.value, currentHigh)
                        if (newSAR < highestOfLast2) {
                            newSAR = highestOfLast2
                        }
                    }
                }

                previousSAR = newSAR
                return newSAR
            }
        }
    }

    override fun updateState(bar: Bar) {
        highPrice.onBar(bar)
        lowPrice.onBar(bar)
        closePrice.onBar(bar)
        previousHigh.onBar(bar)
        previousLow.onBar(bar)
        previousClose.onBar(bar)

        barCount++
        value = calculate()
    }

    override val lag = 2

    override val isStable: Boolean
        get() = barCount >= 2

    override fun toString(): String {
        return "ParabolicSAR(start=$accelerationStart, max=$maxAcceleration, inc=$accelerationIncrement) => $value"
    }
}

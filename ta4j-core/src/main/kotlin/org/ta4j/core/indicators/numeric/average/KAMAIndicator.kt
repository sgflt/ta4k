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
package org.ta4j.core.indicators.numeric.average

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * The Kaufman's Adaptive Moving Average (KAMA) Indicator.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:kaufman_s_adaptive_moving_average](http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:kaufman_s_adaptive_moving_average)
 */
class KAMAIndicator @JvmOverloads constructor(
    numFactory: NumFactory,
    private val price: NumericIndicator,
    private val barCountEffectiveRatio: Int = 10,
    barCountFast: Int = 2,
    barCountSlow: Int = 30,
) : NumericIndicator(price.numFactory) {
    private val fastest: Num
    private val slowest: Num
    private val previousVolatilities = price.difference().abs().runningTotal(barCountEffectiveRatio)
    private val priceAtStartOfRange = price.previous(barCountEffectiveRatio)
    private var barsPassed = 0


    /**
     * Constructor.
     *
     * @param price the price
     * @param barCountEffectiveRatio the time frame of the effective ratio (usually
     * 10)
     * @param barCountFast the time frame fast (usually 2)
     * @param barCountSlow the time frame slow (usually 30)
     */
    /**
     * Constructor with:
     *
     *
     *  * `barCountEffectiveRatio` = 10
     *  * `barCountFast` = 2
     *  * `barCountSlow` = 30
     *
     *
     * @param price the priceindicator
     */
    init {
        val two = numFactory.two()
        fastest = two / numFactory.numOf(barCountFast + 1)
        slowest = two / numFactory.numOf(barCountSlow + 1)
    }


    private fun calculate(): Num {
        val currentPrice = price.value

        /*
         * Efficiency Ratio (ER) ER = Change/Volatility
         * Change = ABS(Close - Close (10 * periods ago))
         * Volatility = Sum10(ABS(Close - Prior Close))
         * Volatility is the sum of the absolute value of the last ten price changes (Close - Prior Close).
         */
        val change = currentPrice.minus(priceAtStartOfRange.value).abs()
        val er = change / previousVolatilities.value
        /*
         * Smoothing Constant (SC) SC = [ER x (fastest SC - slowest SC) + slowest SC]2
         * SC = [ER x (2/(2+1) - 2/(30+1)) + 2/(30+1)]2
         */
        val sc = (er * (fastest - slowest) + slowest).pow(2)


        val priorKAMA = value
        if (barsPassed <= barCountEffectiveRatio) {
            return currentPrice
        }

        /*
         * KAMA Current KAMA = Prior KAMA + SC x (Price - Prior KAMA)
         */
        return priorKAMA.plus(sc * currentPrice.minus(priorKAMA))
    }


    public override fun updateState(bar: Bar) {
        ++barsPassed
        price.onBar(bar)
        priceAtStartOfRange.onBar(bar)
        previousVolatilities.onBar(bar)
        value = calculate()
    }

    override val lag: Int
        get() = barCountEffectiveRatio


    override val isStable: Boolean
        get() = barsPassed >= barCountEffectiveRatio


    override fun toString() = "KAMA($barCountEffectiveRatio) => $value"
}

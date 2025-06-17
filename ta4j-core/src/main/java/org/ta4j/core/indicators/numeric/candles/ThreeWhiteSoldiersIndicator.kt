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
package org.ta4j.core.indicators.numeric.candles

import org.ta4j.core.api.Indicators
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory
import org.ta4j.core.utils.CircularBarArray

/**
 * Three white soldiers indicator.
 *
 * @see [Investopedia](http://www.investopedia.com/terms/t/three_white_soldiers.asp)
 */
class ThreeWhiteSoldiersIndicator(numFactory: NumFactory, barCount: Int, factor: Double) : BooleanIndicator() {
    /** Upper shadow indicators for the three soldiers. */
    private val upperShadows = ArrayList<NumericIndicator>(3)

    /** Average upper shadow. */
    private val averageUpperShadowInd: PreviousNumericValueIndicator

    /** Factor used when checking if a candle has a very short upper shadow. */
    private val factor = numFactory.numOf(factor)

    private val bars = CircularBarArray(5)

    /**
     * Constructor.
     *
     * @param numFactory the number factory
     * @param barCount the number of bars used to calculate the average upper shadow
     * @param factor the factor used when checking if a candle has a very short upper shadow
     */
    init {
        val upperShadowIndicator = Indicators.extended(numFactory).upperShadow()
        upperShadows.add(upperShadowIndicator.previous(3))
        upperShadows.add(upperShadowIndicator.previous(2))
        upperShadows.add(upperShadowIndicator.previous())
        averageUpperShadowInd = upperShadowIndicator.sma(barCount).previous(4)
    }

    private fun calculate(bar: Bar?): Boolean {
        bars.addLast(bar)

        if (bars.isNotFull) {
            // We need 5 candles: 1 black, 3 white soldiers, 1 current being evaluated
            return false
        }

        val index = bars.currentIndex
        val blackCandleIndex = index - 4
        val blackCandle = bars[blackCandleIndex]
        val soldier1 = isWhiteSoldier(index - 3)
        val soldier2 = isWhiteSoldier(index - 2)
        val soldier3 = isWhiteSoldier(index - 1)

        return blackCandle?.isBearish == true && soldier1 && soldier2 && soldier3
    }

    /**
     * @return true if the bar/candle has a very short upper shadow, false otherwise
     */
    private fun hasVeryShortUpperShadow(index: Int): Boolean {
        val currentUpperShadow = upperShadows[index].value
        // We use the black candle index to remove bias of the previous soldiers
        val averageUpperShadow = averageUpperShadowInd.value

        return currentUpperShadow < averageUpperShadow * factor
    }

    /**
     * @param index the current bar/candle index
     * @return true if the current bar/candle is growing, false otherwise
     */
    private fun isGrowing(index: Int): Boolean {
        val (prevOpenPrice, prevClosePrice) = bars[index - 1]?.let { it.openPrice to it.closePrice } ?: return false
        val (currOpenPrice, currClosePrice) = bars[index]?.let { it.openPrice to it.closePrice } ?: return false

        // Opens within the body of the previous candle
        return currOpenPrice > prevOpenPrice
                && currOpenPrice < prevClosePrice
                // Closes above the previous close price
                && currClosePrice > prevClosePrice
    }

    /**
     * @param index the current bar/candle index
     * @return true if the current bar/candle is a white soldier, false otherwise
     */
    private fun isWhiteSoldier(index: Int): Boolean {
        val prevBar = bars[index - 1]
        val currBar = bars[index]
        if (currBar?.isBullish != true) {
            return false
        }

        // Calculate which soldier this is (0, 1, or 2)
        val soldierIndex = index - (bars.currentIndex - 3)

        return if (prevBar?.isBearish == true) {
            // First soldier case
            hasVeryShortUpperShadow(soldierIndex) && currBar.openPrice > prevBar.lowPrice
        } else {
            hasVeryShortUpperShadow(soldierIndex) && isGrowing(index)
        }
    }

    override fun updateState(bar: Bar) {
        averageUpperShadowInd.onBar(bar)
        upperShadows.forEach { it.onBar(bar) }
        value = calculate(bar)
    }

    override val isStable: Boolean
        get() = averageUpperShadowInd.isStable

    override val lag = maxOf(
        5,
        averageUpperShadowInd.lag,
        upperShadows.maxOf { it.lag }
    )
}

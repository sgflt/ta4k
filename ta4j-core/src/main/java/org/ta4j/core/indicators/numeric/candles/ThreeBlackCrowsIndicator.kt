/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
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

import org.ta4j.core.api.Indicators.lowerShadow
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NumFactory
import org.ta4j.core.utils.CircularBarArray

/**
 * Three black crows indicator.
 *
 * @see [
 * http://www.investopedia.com/terms/t/three_black_crows.asp](http://www.investopedia.com/terms/t/three_black_crows.asp)
 */
class ThreeBlackCrowsIndicator(numFactory: NumFactory, barCount: Int, factor: Double) : BooleanIndicator() {
    /** Lower shadow.  */
    private val lowerShadows = ArrayList<NumericIndicator>(3)

    /** Average lower shadow.  */
    private val averageLowerShadowInd: PreviousNumericValueIndicator

    /** Factor used when checking if a candle has a very short lower shadow.  */
    private val factor = numFactory.numOf(factor)

    private val bars = CircularBarArray(4)


    /**
     * Constructor.
     *
     * @param numFactory the bar numFactory
     * @param barCount the number of bars used to calculate the average lower shadow
     * @param factor the factor used when checking if a candle has a very short
     * lower shadow
     */
    init {
        val lowerShadowIndicator = lowerShadow()
        lowerShadows.addLast(lowerShadowIndicator.previous(3))
        lowerShadows.addLast(lowerShadowIndicator.previous(2))
        lowerShadows.addLast(lowerShadowIndicator.previous())
        averageLowerShadowInd = lowerShadowIndicator.sma(barCount).previous(4)
    }


    protected fun calculate(bar: Bar?): Boolean {
        bars.addLast(bar)

        if (bars.isNotFull) {
            // We need 4 candles: 1 white, 3 black
            return false
        }

        val index = bars.currentIndex
        val whiteCandleIndex = index - 3
        return bars[whiteCandleIndex]?.isBullish == true
                && isBlackCrow(index - 2)
                && isBlackCrow(index - 1)
                && isBlackCrow(index)
    }


    /**
     * @return true if the bar/candle has a very short lower shadow, false otherwise
     */
    private fun hasVeryShortLowerShadow(index: Int): Boolean {
        val currentLowerShadow = lowerShadows[index].value
        // We use the white candle index to remove to bias of the previous crows
        val averageLowerShadow = averageLowerShadowInd.value

        return currentLowerShadow.isLessThan(averageLowerShadow.multipliedBy(factor))
    }


    /**
     * @param index the current bar/candle index
     *
     * @return true if the current bar/candle is declining, false otherwise
     */
    private fun isDeclining(index: Int): Boolean {
        val (prevOpenPrice, prevClosePrice) = bars[index - 1]?.let { it.openPrice to it.closePrice } ?: return false
        val (currOpenPrice, currClosePrice) = bars[index]?.let { it.openPrice to it.closePrice } ?: return false

        return currOpenPrice.isLessThan(prevOpenPrice) &&
                currOpenPrice.isGreaterThan(prevClosePrice) &&
                currClosePrice.isLessThan(prevClosePrice)
    }


    /**
     * @param index the current bar/candle index
     *
     * @return true if the current bar/candle is a black crow, false otherwise
     */
    private fun isBlackCrow(index: Int): Boolean {
        val prevBar = bars[index - 1]
        val currBar = bars[index]
        if (currBar?.isBearish != true) {
            return false
        }

        return if (prevBar?.isBullish == true) {
            hasVeryShortLowerShadow(index) && currBar.openPrice.isLessThan(prevBar.highPrice)
        } else {
            hasVeryShortLowerShadow(index) && isDeclining(index)
        }
    }

    override fun updateState(bar: Bar) {
        averageLowerShadowInd.onBar(bar)
        lowerShadows.forEach { it.onBar(bar) }
        value = calculate(bar)
    }


    override val isStable
        get() = averageLowerShadowInd.isStable
}

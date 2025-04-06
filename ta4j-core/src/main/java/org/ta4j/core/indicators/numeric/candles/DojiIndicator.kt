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

import org.ta4j.core.api.Indicators.realBody
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.helpers.TransformIndicator.Companion.abs
import org.ta4j.core.num.NumFactory

/**
 * Doji indicator.
 *
 *
 *
 * A candle/bar is considered Doji if its body height is lower than the average
 * multiplied by a factor.
 *
 * @see [
 * http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.doji](http://stockcharts.com/school/doku.php?id=chart_school:chart_analysis:introduction_to_candlesticks.doji)
 */
class DojiIndicator(numFactory: NumFactory, barCount: Int, bodyFactor: Double) : BooleanIndicator() {
    /** Body height.  */
    private val bodyHeightInd = abs(realBody())

    /** Average body height.  */
    private val averageBodyHeightInd = bodyHeightInd.sma(barCount).previous()

    /** The factor used when checking if a candle is Doji.  */
    private val factor = numFactory.numOf(bodyFactor)

    private var _isStable: Boolean = false
    override var value: Boolean = false
        set(value) {
            _isStable = true
            super.value = value
        }


    private fun calculate(): Boolean {
        val averageBodyHeight = averageBodyHeightInd.value
        val currentBodyHeight = bodyHeightInd.value

        return currentBodyHeight.isLessThan(averageBodyHeight.multipliedBy(factor))
    }


    public override fun updateState(bar: Bar) {
        bodyHeightInd.onBar(bar)
        averageBodyHeightInd.onBar(bar)
        value = calculate()
    }


    override val isStable: Boolean
        get() = _isStable
}

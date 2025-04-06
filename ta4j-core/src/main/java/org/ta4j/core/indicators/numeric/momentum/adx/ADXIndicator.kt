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
package org.ta4j.core.indicators.numeric.momentum.adx

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.num.NumFactory

/**
 * ADX indicator.
 *
 *
 *
 * Part of the Directional Movement System.
 *
 * @see [https://www.investopedia.com/terms/a/adx.asp](https://www.investopedia.com/terms/a/adx.asp)
 */
class ADXIndicator(numFactory: NumFactory, private val diBarCount: Int, private val adxBarCount: Int) :
    NumericIndicator(numFactory) {
    private val averageDXIndicator = MMAIndicator(DXIndicator(numFactory, diBarCount), adxBarCount)


    /**
     * Constructor.
     *
     * @param numFactory the bar numFactory
     * @param barCount the bar count for [DXIndicator] and
     * [.averageDXIndicator]
     */
    constructor(numFactory: NumFactory, barCount: Int) : this(numFactory, barCount, barCount)


    override fun toString(): String {
        return javaClass.getSimpleName() + " diBarCount: " + this.diBarCount + " adxBarCount: " + this.adxBarCount
    }


    private fun calculate() = averageDXIndicator.value


    override fun updateState(bar: Bar) {
        averageDXIndicator.onBar(bar)
        value = calculate()
    }


    override val isStable
        get() = averageDXIndicator.isStable
}

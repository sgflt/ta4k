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
package org.ta4j.core.indicators.numeric

import org.ta4j.core.api.series.Bar
import org.ta4j.core.num.NumFactory

/**
 * Close Location Value (CLV) indicator.
 *
 * @see [Close Location Value](https://www.investopedia.com/terms/c/close_location_value.asp)
 */
class CloseLocationValueIndicator(numFactory: NumFactory) : NumericIndicator(numFactory) {
    private fun calculate(bar: Bar) = with(bar) {
        val diffHighLow = highPrice.minus(lowPrice)

        if (diffHighLow.isNaN || diffHighLow.isZero) numFactory.zero()
        else closePrice.minus(lowPrice).minus(highPrice.minus(closePrice)).dividedBy(diffHighLow)
    }

    override fun updateState(bar: Bar) {
        value = calculate(bar)
    }

    override val isStable
        get() = !value.isNaN
}

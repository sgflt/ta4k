/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and  permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric.candles.price

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.SeriesRelatedNumericIndicator
import org.ta4j.core.num.NumFactory

/**
 * Average high-low indicator.
 *
 *
 *
 * Returns the median price of a bar using the following formula:
 *
 * <pre>
 * MedianPrice = (highPrice + lowPrice) / 2
</pre> *
 */
class MedianPriceIndicator(numFactory: NumFactory) : SeriesRelatedNumericIndicator(numFactory) {
    private fun calculate(bar: Bar) = bar.highPrice.plus(bar.lowPrice).dividedBy(numFactory.two())

    public override fun updateState(bar: Bar) {
        value = calculate(bar)
    }
}

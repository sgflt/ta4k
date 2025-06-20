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
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.ta4j.core.indicators.numeric.NumericIndicator

/**
 * A facade to create the 3 Bollinger Band indicators. A simple moving average
 * of close price is used as the middle band. The BB bandwidth and %B indicators
 * can also be created on demand.
 *
 *
 *
 * This class creates lightweight "fluent" numeric indicators. These objects are
 * not cached, although they may be wrapped around cached objects. Overall there
 * is less caching and probably better performance.
 */
class BollingerBandFacade(
    price: NumericIndicator,
    barCount: Int,
    k: Number,
) {
    val middle: NumericIndicator = price.sma(barCount)
    val upper: NumericIndicator = middle.plus(price.stddev(barCount).multipliedBy(k))
    val lower: NumericIndicator = middle.minus(price.stddev(barCount).multipliedBy(k))
    val bandwidth: NumericIndicator = upper.minus(lower).dividedBy(middle).multipliedBy(100)
    val percentB: NumericIndicator = price.minus(lower).dividedBy(upper.minus(lower))
}

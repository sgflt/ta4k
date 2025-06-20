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
package org.ta4j.core.indicators.numeric.channels.keltner

import org.ta4j.core.api.Indicators.atr
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.EMAIndicator

/**
 * A facade to create the 3 Keltner Channel indicators. An exponential moving
 * average of close price is used as the middle channel.
 *
 *
 *
 * This class creates lightweight "fluent" numeric indicators. These objects are
 * not cached, although they may be wrapped around cached objects. Overall there
 * is less caching and probably better performance.
 */
class KeltnerChannelFacade(emaCount: Int, atrCount: Int, k: Number) {
    private val middle: EMAIndicator
    private val upper: NumericIndicator
    private val lower: NumericIndicator


    /**
     * Constructor.
     *
     * @param emaCount the bar count for the `EmaIndicator`
     * @param atrCount the bar count for the `ATRIndicator`
     * @param k the multiplier for the [.upper] and [.lower]
     * channel
     */
    init {
        val price = closePrice()
        val atr = atr(atrCount)
        this.middle = price.ema(emaCount)
        this.upper = this.middle.plus(atr.multipliedBy(k))
        this.lower = this.middle.minus(atr.multipliedBy(k))
    }


    /** @return the middle channel
     */
    fun middle(): EMAIndicator {
        return this.middle
    }


    /** @return the upper channel
     */
    fun upper(): NumericIndicator {
        return this.upper
    }


    /** @return the lower channel
     */
    fun lower(): NumericIndicator {
        return this.lower
    }
}

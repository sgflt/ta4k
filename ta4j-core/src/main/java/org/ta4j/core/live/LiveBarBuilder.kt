/**
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
package org.ta4j.core.live

import org.ta4j.core.api.series.BarBuilder
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import java.time.Instant

/**
 * A builder to build a new `LiveBar` with conversion from a
 * [Number] of type `T` to a [Num implementation][Num].
 */
internal class LiveBarBuilder(private val series: BarSeries) : BarBuilder {
    private val numFactory: NumFactory = series.numFactory
    private var startTime: Instant = Instant.EPOCH
    private var endTime: Instant = Instant.EPOCH
    private var openPrice: Num = NaN
    private var highPrice: Num = NaN
    private var lowPrice: Num = NaN
    private var closePrice: Num = NaN
    private var volume: Num = NaN


    override fun startTime(startTime: Instant): LiveBarBuilder {
        this.startTime = startTime
        return this
    }


    override fun endTime(endTime: Instant): LiveBarBuilder {
        this.endTime = endTime
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    override fun openPrice(openPrice: Number): LiveBarBuilder {
        this.openPrice = this.numFactory.numOf(openPrice)
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    fun openPrice(openPrice: String): LiveBarBuilder {
        this.openPrice = this.numFactory.numOf(openPrice)
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    override fun highPrice(highPrice: Number): LiveBarBuilder {
        this.highPrice = this.numFactory.numOf(highPrice)
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    fun highPrice(highPrice: String): LiveBarBuilder {
        this.highPrice = this.numFactory.numOf(highPrice)
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    override fun lowPrice(lowPrice: Number): LiveBarBuilder {
        this.lowPrice = this.numFactory.numOf(lowPrice)
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    fun lowPrice(lowPrice: String): LiveBarBuilder {
        this.lowPrice = this.numFactory.numOf(lowPrice)
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    override fun closePrice(closePrice: Number): LiveBarBuilder {
        this.closePrice = this.numFactory.numOf(closePrice)
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    fun closePrice(closePrice: String): LiveBarBuilder {
        this.closePrice = this.numFactory.numOf(closePrice)
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    override fun volume(volume: Number): LiveBarBuilder {
        this.volume = this.numFactory.numOf(volume)
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    fun volume(volume: String): LiveBarBuilder {
        this.volume = this.numFactory.numOf(volume)
        return this
    }


    fun build(): LiveBar {
        return LiveBar(
            startTime,
            endTime,
            openPrice,
            highPrice,
            lowPrice,
            closePrice,
            volume
        )
    }


    override fun add() {
        series.addBar(build())
    }
}

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
package org.ta4j.core.backtest

import org.ta4j.core.api.series.BarBuilder
import org.ta4j.core.api.series.BarSeries
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import java.time.Instant

/**
 * A builder to build a new [BacktestBar].
 */
open class BacktestBarBuilder(private val barSeries: BarSeries) : BarBuilder {
    private var startTime: Instant? = null
    private var endTime: Instant? = null
    private var openPrice: Num = NaN
    private var highPrice: Num = NaN
    private var lowPrice: Num = NaN
    private var closePrice: Num = NaN
    private var volume: Num = NaN
    private var amount: Num = barSeries.numFactory.zero()
    private var trades: Long = 0
    private val numFactory: NumFactory = barSeries.numFactory


    fun trades(trades: String): BacktestBarBuilder {
        trades(trades.toLong())
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    override fun openPrice(openPrice: Number): BacktestBarBuilder {
        openPrice(numFactory.numOf(openPrice))
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    fun openPrice(openPrice: String): BacktestBarBuilder {
        openPrice(numFactory.numOf(openPrice))
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    override fun highPrice(highPrice: Number): BacktestBarBuilder {
        highPrice(numFactory.numOf(highPrice))
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    fun highPrice(highPrice: String): BacktestBarBuilder {
        highPrice(numFactory.numOf(highPrice))
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    override fun lowPrice(lowPrice: Number): BacktestBarBuilder {
        lowPrice(numFactory.numOf(lowPrice))
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    fun lowPrice(lowPrice: String): BacktestBarBuilder {
        lowPrice(numFactory.numOf(lowPrice))
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    override fun closePrice(closePrice: Number): BacktestBarBuilder {
        closePrice(numFactory.numOf(closePrice))
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    fun closePrice(closePrice: String): BacktestBarBuilder {
        closePrice(numFactory.numOf(closePrice))
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    override fun volume(volume: Number): BacktestBarBuilder {
        volume(numFactory.numOf(volume))
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    fun volume(volume: String): BacktestBarBuilder {
        volume(numFactory.numOf(volume))
        return this
    }


    /**
     * @param amount the total traded amount of the bar period
     *
     * @return `this`
     */
    fun amount(amount: Number): BacktestBarBuilder {
        amount(numFactory.numOf(amount))
        return this
    }


    /**
     * @param amount the total traded amount of the bar period
     *
     * @return `this`
     */
    fun amount(amount: String): BacktestBarBuilder {
        amount(numFactory.numOf(amount))
        return this
    }


    /**
     * @param startTime the end time of the bar period
     *
     * @return `this`
     */
    override fun startTime(startTime: Instant): BacktestBarBuilder {
        this.startTime = startTime
        return this
    }


    /**
     * @param endTime the end time of the bar period
     *
     * @return `this`
     */
    override fun endTime(endTime: Instant): BacktestBarBuilder {
        this.endTime = endTime
        return this
    }


    /**
     * @param openPrice the open price of the bar period
     *
     * @return `this`
     */
    fun openPrice(openPrice: Num): BacktestBarBuilder {
        this.openPrice = openPrice
        return this
    }


    /**
     * @param highPrice the highest price of the bar period
     *
     * @return `this`
     */
    fun highPrice(highPrice: Num): BacktestBarBuilder {
        this.highPrice = highPrice
        return this
    }


    /**
     * @param lowPrice the lowest price of the bar period
     *
     * @return `this`
     */
    fun lowPrice(lowPrice: Num): BacktestBarBuilder {
        this.lowPrice = lowPrice
        return this
    }


    /**
     * @param closePrice the close price of the bar period
     *
     * @return `this`
     */
    fun closePrice(closePrice: Num): BacktestBarBuilder {
        this.closePrice = closePrice
        return this
    }


    /**
     * @param volume the total traded volume of the bar period
     *
     * @return `this`
     */
    fun volume(volume: Num): BacktestBarBuilder {
        this.volume = volume
        return this
    }


    /**
     * @param amount the total traded amount of the bar period
     *
     * @return `this`
     */
    fun amount(amount: Num): BacktestBarBuilder {
        this.amount = amount
        return this
    }


    /**
     * @param trades the number of trades of the bar period
     *
     * @return `this`
     */
    fun trades(trades: Long): BacktestBarBuilder {
        this.trades = trades
        return this
    }


    open fun build(): BacktestBar {
        val backtestBar = BacktestBar(
            startTime!!,
            endTime!!,
            openPrice,
            highPrice,
            lowPrice,
            closePrice,
            volume,
            amount,
            trades
        )
        reset()
        return backtestBar
    }


    private fun reset() {
        startTime = null
        endTime = null
        openPrice = NaN
        highPrice = NaN
        lowPrice = NaN
        closePrice = NaN
        volume = NaN
        amount = NaN
        trades = 0
    }


    override fun add() {
        barSeries.addBar(build())
    }
}

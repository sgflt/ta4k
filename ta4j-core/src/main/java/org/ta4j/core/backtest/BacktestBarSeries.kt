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
package org.ta4j.core.backtest

import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.*
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Base implementation of a [BarSeries].
 *
 *
 *  * limited to single timeframe
 *
 */
class BacktestBarSeries internal constructor(
    /** The name of the bar series.  */
    override val name: String,
    override val timeFrame: TimeFrame,
    override val numFactory: NumFactory,
    barBuilderFactory: BarBuilderFactory,
    barListeners: List<BarListener>,
) : BarSeries {
    /**
     * Returns the raw bar data, i.e. it returns the current list object, which is
     * used internally to store the [Bar]. It may be:
     *
     *
     *  * a shortened bar list if a `maximumBarCount` has been set.
     *  * an extended bar list if it is a constrained bar series.
     *
     * @return the raw bar data
     */
    val barData: MutableList<BacktestBar> = ArrayList<BacktestBar>()
    private val barBuilderFactory: BarBuilderFactory = Objects.requireNonNull<BarBuilderFactory>(barBuilderFactory)
    private val barListeners: MutableList<BarListener> = ArrayList<BarListener>(1).apply { addAll(barListeners) }

    /**
     * Where we are located now
     */
    var currentIndex: Int = -1
        private set


    /**
     * Advances time to next bar.
     *
     * Notifies strategies to onBar their state
     *
     * @return true if advanced to next bar
     */
    private fun advance(): Boolean {
        if (canAdvance()) {
            ++currentIndex

            for (barListener in barListeners) {
                barListener.onBar(bar)
            }
            return true
        }

        return false
    }


    private fun canAdvance(): Boolean {
        return currentIndex < endIndex
    }


    override val currentTime: Instant
        get() = barData.last().endTime

    override fun barBuilder(): BacktestBarBuilder {
        return barBuilderFactory.createBarBuilder(this) as BacktestBarBuilder
    }


    override val bar: BacktestBar
        get() = barData[currentIndex]


    fun getBar(index: Int): Bar {
        return barData[index]
    }


    override fun onCandle(event: CandleReceived) {
        checkTimeFrame(event)

        barBuilder().apply {
            startTime(event.beginTime)
            endTime(event.beginTime)
            event.openPrice.takeIf { !it.isNaN() }?.let { openPrice(it) }
            event.highPrice.takeIf { !it.isNaN() }?.let { highPrice(it) }
            event.lowPrice.takeIf { !it.isNaN() }?.let { lowPrice(it) }
            event.closePrice.takeIf { !it.isNaN() }?.let { closePrice(it) }
            event.volume.takeIf { !it.isNaN() }?.let { volume(it) }
        }.add()
    }


    private fun checkTimeFrame(candleReceived: CandleReceived) {
        if (candleReceived.timeFrame != timeFrame) {
            throw WrongTimeFrameException(
                timeFrame,
                candleReceived.timeFrame
            )
        }
    }


    val barCount: Int
        /**
         * @return the number of bars in the series
         */
        get() = barData.size


    val isEmpty: Boolean
        /**
         * @return true if the series is empty, false otherwise
         */
        get() = barCount == 0


    val beginIndex: Int
        /**
         * @return the begin index of the series
         */
        get() = 0


    val endIndex: Int
        /**
         * @return the end index of the series
         */
        get() = barData.size - 1


    /**
     * Adds the `bar` at the end of the series.
     *
     *
     *
     * The `beginIndex` is set to `0` if not already initialized.<br></br>
     * The `endIndex` is set to `0` if not already initialized, or
     * incremented if it matches the end of the series.<br></br>
     * Exceeding bars are removed.
     *
     * @param bar the bar to be added
     *
     * @throws NullPointerException if `bar` is `null`
     */
    override fun addBar(bar: Bar) {
        Objects.requireNonNull<Bar?>(bar, "bar must not be null")
        require(bar is BacktestBar) { "Wrong bar type: " + bar.closePrice.name }

        if (barData.isNotEmpty()) {
            val seriesEndTime = currentTime
            if (!bar.endTime.isAfter(seriesEndTime)) {
                throw PastCandleParadoxException(bar.endTime, seriesEndTime)
            }
        }

        barData.add(bar)
        advance()
    }


    /**
     * Adds a trade and updates the close price of the last bar.
     *
     * @param amount the traded volume
     * @param price the price
     *
     * @see BacktestBar.addTrade
     */
    fun addTrade(price: Number, amount: Number) {
        addTrade(numFactory.numOf(price), numFactory.numOf(amount))
    }


    /**
     * Adds a trade and updates the close price of the last bar.
     *
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     *
     * @see BacktestBar.addTrade
     */
    fun addTrade(tradeVolume: Num, tradePrice: Num) {
        lastBar.addTrade(tradeVolume, tradePrice)
    }


    val lastBar
        get() = barData.last()


    val firstBar
        get() = barData.first()


    /**
     * Updates the close price of the last bar. The open, high and low prices are
     * also updated as needed.
     *
     * @param price the price for the bar
     *
     * @see BacktestBar.addPrice
     */
    fun addPrice(price: Num) {
        lastBar.addPrice(price)
    }


    fun rewind() {
        currentIndex = -1
    }


    val seriesPeriodDescription: String
        /**
         * @return the description of the series period (e.g. "from 12:00 21/01/2014 to
         * 12:15 21/01/2014")
         */
        get() {
            val sb = StringBuilder()
            if (barData.isNotEmpty()) {
                val firstBar: Bar = firstBar
                val lastBar: Bar = lastBar
                sb.append(firstBar.endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME))
                    .append(" - ")
                    .append(lastBar.endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME))
            }
            return sb.toString()
        }


    override fun addBarListener(listener: BarListener) {
        barListeners.add(listener)
    }


    fun clearListeners() {
        barListeners.clear()
    }
}

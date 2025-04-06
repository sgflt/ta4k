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

import org.ta4j.core.api.series.Bar
import org.ta4j.core.num.Num
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.function.Function

data class BacktestBar(
    override val beginTime: Instant,
    override val endTime: Instant,
    override var openPrice: Num,
    override var highPrice: Num,
    override var lowPrice: Num,
    override var closePrice: Num,
    override var volume: Num,
    var amount: Num,
    var trades: Long,
) : Bar {
    override val timePeriod: Duration = Duration.between(beginTime, endTime)


    fun addTrade(tradeVolume: Num, tradePrice: Num) {
        addPrice(tradePrice)

        volume = volume.plus(tradeVolume)
        amount = amount.plus(tradeVolume.multipliedBy(tradePrice))
        trades++
    }


    /**
     * Updates the close price at the end of the bar period. The open, high and low
     * prices are also updated as needed.
     *
     * @param price the actual price per asset
     * @param numFunction the numbers precision
     */
    fun addPrice(price: String, numFunction: Function<Number?, Num?>) {
        addPrice(numFunction.apply(BigDecimal(price))!!)
    }


    /**
     * Updates the close price at the end of the bar period. The open, high and low
     * prices are also updated as needed.
     *
     * @param price the actual price per asset
     * @param numFunction the numbers precision
     */
    fun addPrice(price: Number?, numFunction: Function<Number?, Num?>) {
        addPrice(numFunction.apply(price)!!)
    }


    /**
     * Updates the close price at the end of the bar period. The open, high and low
     * prices are also updated as needed.
     *
     * @param price the actual price per asset
     */
    fun addPrice(price: Num) {
        if (openPrice.isNaN) {
            openPrice = price
        }
        closePrice = price
        highPrice = when {
            highPrice.isNaN -> price
            highPrice.isLessThan(price) -> price
            else -> highPrice
        }
        lowPrice = when {
            lowPrice.isNaN -> price
            lowPrice.isGreaterThan(price) -> price
            else -> lowPrice
        }
    }

    override fun toString(): String =
        "Bar(beginTime=$beginTime, endTime=$endTime, closePrice=${closePrice.doubleValue()}, " +
                "openPrice=${openPrice.doubleValue()}, lowPrice=${lowPrice.doubleValue()}, " +
                "highPrice=${highPrice.doubleValue()}, volume=${volume.doubleValue()})"
}

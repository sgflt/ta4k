/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.function.Function
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.Num

data class BacktestBar(
    override val timeFrame: TimeFrame,
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

        volume = volume + tradeVolume
        amount = amount + tradeVolume * tradePrice
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
            highPrice < price -> price
            else -> highPrice
        }
        lowPrice = when {
            lowPrice.isNaN -> price
            lowPrice > price -> price
            else -> lowPrice
        }
    }

    override fun toString(): String =
        "Bar(beginTime=$beginTime, endTime=$endTime, closePrice=${closePrice.doubleValue()}, " +
                "openPrice=${openPrice.doubleValue()}, lowPrice=${lowPrice.doubleValue()}, " +
                "highPrice=${highPrice.doubleValue()}, volume=${volume.doubleValue()})"
}

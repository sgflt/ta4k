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
package org.ta4j.core.backtest

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import org.ta4j.core.TradeType
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.backtest.analysis.CashFlow
import org.ta4j.core.backtest.analysis.Returns
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * A `Position` is a pair of two [trades][Trade].
 *
 *
 *
 * The exit trade has the complement type of the entry trade, i.e.:
 *
 *  * entry == BUY --> exit == SELL
 *  * entry == SELL --> exit == BUY
 *
 */
class Position @JvmOverloads constructor(
    /** The type of the entry trade  */
    private val startingType: TradeType = TradeType.BUY,
    /** The cost model for transactions of the asset  */
    private val transactionCostModel: CostModel = ZeroCostModel,

    /** The cost model for holding the asset  */
    private val holdingCostModel: CostModel = ZeroCostModel,
    internal val numFactory: NumFactory,
) : BarListener {
    /** The entry trade  */
    var entry: Trade? = null
        private set

    /** The exit trade  */
    var exit: Trade? = null
        private set

    private val priceHistory = PositionHistory()


    override fun equals(other: Any?): Boolean =
        other is Position && entry == other.entry && exit == other.exit


    override fun hashCode(): Int {
        return Objects.hash(entry, exit)
    }


    /**
     * Operates the position at the index-th position.
     *
     * @param whenExecuted the executed bar
     * @param amount the amount
     *
     * @return the trade
     *
     * @throws IllegalStateException if [.isOpened]
     */
    fun operate(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Trade {
        val trade = Trade(
            whenExecuted = whenExecuted,
            pricePerAsset = pricePerAsset,
            type = startingType,
            amount = amount,
            transactionCostModel = transactionCostModel,
            orderType = if (isNew) Trade.OrderType.OPEN else Trade.OrderType.CLOSE
        )

        if (isNew) {
            entry = trade
        } else if (isOpened) {
            exit = trade
        }

        return trade
    }


    private val isLong: Boolean
        get() = entry?.isBuy == true


    val isClosed: Boolean
        /**
         * @return true if the position is closed, false otherwise
         */
        get() = (entry != null) && (exit != null)


    val isOpened
        /**
         * @return true if the position is opened, false otherwise
         */
        get() = entry != null && exit == null


    val isNew
        /**
         * @return true if the position is new, false otherwise
         */
        get() = entry == null && exit == null


    /**
     * @return true if position is closed and [.getProfit] > 0
     */
    fun hasProfit(): Boolean {
        return profit.isPositive
    }


    /**
     * @return true if position is closed and [.getProfit] < 0
     */
    fun hasLoss(): Boolean {
        return profit.isNegative
    }


    val profit: Num
        /**
         * Calculates the net profit of the position if it is closed. The net profit
         * includes any trading costs.
         *
         * @return the profit or loss of the position
         */
        get() {
            return if (isOpened) {
                zero()
            } else {
                getGrossProfit(exit!!.pricePerAsset).minus(positionCost)
            }
        }


    /**
     * Calculates the net profit of the position. If it is open, calculates the
     * profit until the final bar. The net profit includes any trading costs.
     *
     * @param finalIndex the index of the final bar to be considered (if position is
     * open)
     * @param finalPrice the price of the final bar to be considered (if position is
     * open)
     *
     * @return the profit or loss of the position
     */
    fun getProfit(finalIndex: Int, finalPrice: Num): Num {
        val grossProfit = getGrossProfit(finalPrice)
        val tradingCost = getPositionCost(finalIndex)
        return grossProfit.minus(tradingCost)
    }


    val grossProfit: Num
        /**
         * Calculates the gross profit of the position if it is closed. The gross profit
         * excludes any trading costs.
         *
         * @return the gross profit of the position
         */
        get() {
            return if (isOpened) {
                zero()
            } else {
                getGrossProfit(exit!!.pricePerAsset)
            }
        }


    /**
     * Calculates the gross profit of the position. The gross profit excludes any
     * trading costs.
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     * open)
     *
     * @return the profit or loss of the position
     */
    fun getGrossProfit(finalPrice: Num): Num {
        var grossProfit: Num
        if (isOpened) {
            grossProfit = entry!!.amount * finalPrice - entry!!.value
        } else {
            grossProfit = exit!!.value - entry!!.value
        }

        // Profits of long position are losses of short
        if (entry!!.isSell) {
            grossProfit = -grossProfit
        }
        return grossProfit
    }


    val grossReturn: Num
        /**
         * Calculates the gross return of the position if it is closed. The gross return
         * excludes any trading costs (and includes the base).
         *
         * @return the gross return of the position in percent
         *
         * @see .getGrossReturn
         */
        get() {
            return if (isOpened) {
                zero()
            } else {
                getGrossReturn(exit!!.pricePerAsset)
            }
        }


    /**
     * Calculates the gross return of the position, if it exited at the provided
     * price. The gross return excludes any trading costs (and includes the base).
     *
     * @param finalPrice the price of the final bar to be considered (if position is
     * open)
     *
     * @return the gross return of the position in percent
     *
     * @see .getGrossReturn
     */
    fun getGrossReturn(finalPrice: Num): Num {
        return getGrossReturn(entry!!.pricePerAsset, finalPrice)
    }


    /**
     * Calculates the gross return between entry and exit price in percent. Includes
     * the base.
     *
     *
     *
     * For example:
     *
     *  * For buy position with a profit of 4%, it returns 1.04 (includes the base)
     *  * For sell position with a loss of 4%, it returns 0.96 (includes the base)
     *
     *
     * @param entryPrice the entry price
     * @param exitPrice the exit price
     *
     * @return the gross return in percent between entryPrice and exitPrice
     * (includes the base)
     */
    fun getGrossReturn(entryPrice: Num, exitPrice: Num): Num {
        return if (entry?.isBuy == true) {
            exitPrice / entryPrice
        } else {
            val one = entryPrice.numFactory.one()
            (-(exitPrice / entryPrice - one)) + one
        }
    }


    /**
     * Calculates the total cost of the position.
     *
     * @param finalIndex the index of the final bar to be considered (if position is
     * open)
     *
     * @return the cost of the position
     */
    fun getPositionCost(finalIndex: Int): Num {
        val transactionCost = transactionCostModel.calculate(this, finalIndex)
        val borrowingCost = getHoldingCost(finalIndex)
        return transactionCost.plus(borrowingCost)
    }


    val positionCost: Num
        /**
         * Calculates the total cost of the closed position.
         *
         * @return the cost of the position
         */
        get() {
            val transactionCost = transactionCostModel.calculate(this)
            val borrowingCost = holdingCost
            return transactionCost.plus(borrowingCost)
        }


    val holdingCost: Num
        /**
         * Calculates the holding cost of the closed position.
         *
         * @return the cost of the position
         */
        get() = holdingCostModel.calculate(this)


    /**
     * Calculates the holding cost of the position.
     *
     * @param finalIndex the index of the final bar to be considered (if position is
     * open)
     *
     * @return the cost of the position
     */
    fun getHoldingCost(finalIndex: Int): Num {
        return holdingCostModel.calculate(this, finalIndex)
    }


    /**
     * @return the Num of 0
     */
    private fun zero(): Num {
        return entry!!.netPrice.numFactory.zero()
    }


    override fun onBar(bar: Bar) {
        if (isOpened) {
            priceHistory.onBar(bar)
        }
    }


    val cashFlow
        get() = priceHistory.cashFlow


    override fun toString(): String {
        return "Entry: " + entry + " exit: " + exit
    }


    val maxDrawdown: Num
        /**
         * Calculates the maximum drawdown from a cash flow over a series.
         *
         * The formula is as follows:
         *
         * <pre>
         * MDD = (LP - PV) / PV
         * with MDD: Maximum drawdown, in percent.
         * with LP: Lowest point (lowest value after peak value).
         * with PV: Peak value (highest value within the observation).
        </pre> *
         *
         * @return the maximum drawdown from a cash flow over a series
         */
        get() {
            val values = CashFlow(this).values
            if (values.isEmpty()) {
                log.debug("No values in cash flow - returning zero drawdown")
                return numFactory.zero()
            }

            var maxPeak = numFactory.one()
            var maximumDrawdown = numFactory.zero()

            log.debug(
                "Calculating maximum drawdown for {} position",
                if (isLong) "long" else "short"
            )

            for (entry in values.entries) {
                val time = entry.key
                val value = entry.value
                val adjustedValue: Num = if (isLong) value else numFactory.one() / value

                log.debug(
                    "Processing value at {}: raw={}, adjusted={}",
                    time,
                    value,
                    adjustedValue
                )

                // Update peak if we have a new high
                if (adjustedValue > maxPeak) {
                    log.debug("New peak found: {} -> {}", maxPeak, adjustedValue)
                    maxPeak = adjustedValue
                }

                // Calculate drawdown from peak
                val drawdown = (maxPeak - adjustedValue) / maxPeak
                log.debug(
                    "Current drawdown: {} (peak={}, value={})",
                    drawdown,
                    maxPeak,
                    adjustedValue
                )

                if (drawdown > maximumDrawdown) {
                    log.debug(
                        "New maximum drawdown: {} -> {}",
                        maximumDrawdown,
                        drawdown
                    )
                    maximumDrawdown = drawdown
                }
            }

            log.debug("Final maximum drawdown: {}", maximumDrawdown)
            return maximumDrawdown
        }


    /**
     * Calculates returns for the position using specified return type.
     *
     * @param returnType type of return calculation to use
     *
     * @return Returns instance representing position returns over time
     */
    fun getReturns(returnType: Returns.ReturnType): NavigableMap<Instant, Num> {
        if (priceHistory.data.isEmpty()) {
            return TreeMap<Instant, Num>() // Empty returns if no price history
        }

        val returns = TreeMap<Instant, Num>()
        val isLongTrade = entry!!.isBuy
        val holdingCost = holdingCost
        var previousPrice = entry!!.netPrice

        for (bar in priceHistory.data) {
            val currentPrice = bar.closePrice
            val adjustedPrice = if (isLongTrade) currentPrice.minus(holdingCost) else currentPrice.plus(holdingCost)

            val assetReturn = returnType.calculate(adjustedPrice, previousPrice, numFactory)
            val strategyReturn = if (isLongTrade) assetReturn else -assetReturn

            returns.put(bar.endTime, strategyReturn)
            previousPrice = currentPrice
        }

        return returns
    }


    fun getTimeInTrade(chronoUnit: ChronoUnit): Long {
        if (isClosed) {
            return chronoUnit.between(entry!!.whenExecuted, exit!!.whenExecuted)
        }

        if (isOpened && priceHistory.data.isNotEmpty()) {
            return chronoUnit.between(entry!!.whenExecuted, priceHistory.data.last().endTime)
        }

        return 0L
    }


    /**
     * @param `when` at which time point we measure value
     * @param value at time point
     */
    @JvmRecord
    data class PositionValue(val `when`: Instant, val value: Num)

    /**
     * accumulates bars during lifetime of position
     */
    inner class PositionHistory : BarListener {
        internal val data = mutableListOf<Bar>()


        val cashFlow
            get() = data.map { bar ->
                val currentPrice = bar.closePrice
                val adjustedPrice =
                    if (isLong)
                    // FIXME holding cost is over night, usually not during day
                        currentPrice.minus(holdingCost)
                    else
                        currentPrice.plus(holdingCost)

                val ratio = calculatePriceRatio(isLong, entry!!.pricePerAsset, adjustedPrice)
                PositionValue(bar.endTime, ratio)
            }


        private fun calculatePriceRatio(isLongTrade: Boolean, entryPrice: Num, currentPrice: Num): Num {
            if (isLongTrade) {
                return currentPrice / entryPrice
            }

            // For short positions
            return numFactory.one() + (entryPrice - currentPrice) / entryPrice
        }


        override fun onBar(bar: Bar) {
            data.add(bar)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(Position::class.java)
    }
}

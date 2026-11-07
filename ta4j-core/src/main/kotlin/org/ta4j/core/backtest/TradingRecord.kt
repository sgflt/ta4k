/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.analysis.EquityPoint
import org.ta4j.core.num.Num

/**
 * A `TradingRecord` holds the full history/record of a trading session
 * when running a [strategy][Strategy]. It can be used to:
 *
 *
 *  * analyze the performance of a [strategy][Strategy]
 *  * check whether some [rules][Rule] are satisfied (while running a
 * strategy)
 *
 */
interface TradingRecord {
    /**
     * @return the entry type (BUY or SELL) of the first trade in the trading
     * session
     */
    val startingType: TradeType

    /**
     * @return the name of the TradingRecord
     */
    val name: String

    /**
     * Places an entry trade in the trading record.
     *
     * @param pricePerAsset the trade price per asset
     * @param amount the trade amount
     *
     * @return true if the entry has been placed, false otherwise
     */
    fun enter(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Boolean

    /**
     * Places an exit trade in the trading record.
     *
     * @param pricePerAsset the trade price per asset
     * @param amount the trade amount
     *
     * @return true if the exit has been placed, false otherwise
     */
    fun exit(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Boolean

    val isClosed: Boolean
        /**
         * @return true if no position is open, false otherwise
         */
        get() = !this.currentPosition!!.isOpened

    /**
     * @return the recorded closed positions
     */
    val positions: MutableList<Position>

    val positionCount: Int
        /**
         * @return the number of recorded closed positions
         */
        get() = this.positions.size

    /**
     * @return the current (open) position
     */
    val currentPosition: Position

    val lastPosition: Position?
        /**
         * @return the last closed position recorded
         */
        get() {
            val positions = this.positions
            if (positions.isNotEmpty()) {
                return positions.last()
            }
            return null
        }

    val maximumDrawdown: Num?

    /**
     * @return the last trade recorded
     */
    val lastTrade: Trade?

    /**
     * @param tradeType the type of the trade to get the last of
     *
     * @return the last trade (of the provided type) recorded
     */
    fun getLastTrade(tradeType: TradeType?): Trade?

    /**
     * @return the last entry trade recorded
     */
    val lastEntry: Trade?

    /**
     * @return the last exit trade recorded
     */
    val lastExit: Trade?

    val isEmpty: Boolean

    /**
     * Equity at the end of every bar, flat periods included. Empty when the record does not track it.
     */
    val equityCurve: List<EquityPoint>
        get() = emptyList()
}

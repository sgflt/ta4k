/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.analysis

import java.time.Instant
import org.ta4j.core.backtest.Position
import org.ta4j.core.num.Num

/**
 * Equity of a strategy at the end of a bar, relative to a starting equity of 1.
 *
 * @param invested whether a position was open at that time
 */
data class EquityPoint(val time: Instant, val equity: Num, val invested: Boolean)

/**
 * Records equity bar by bar, including bars without an open position, so return-series
 * criteria (Sharpe, Sortino, ...) see flat periods too.
 *
 * Closed positions compound net of costs; an open position is marked to the bar's close
 * net of its entry cost.
 */
class EquityCurve {
    private val _points = mutableListOf<EquityPoint>()
    val points: List<EquityPoint>
        get() = _points

    private var realizedGrowth: Num? = null

    /** Compounds the growth of a closed position into the realized equity. */
    fun onPositionClosed(position: Position) {
        val growth = growth(position.profit, position)
        realizedGrowth = realizedGrowth?.times(growth) ?: growth
    }

    /** Appends the equity at [time]; ignores times not after the last recorded one. */
    fun record(time: Instant, closePrice: Num, openPosition: Position) {
        if (_points.isNotEmpty() && !time.isAfter(_points.last().time)) {
            return
        }
        val one = closePrice.numFactory.one()
        val realized = realizedGrowth ?: one
        val equity = if (openPosition.isOpened) {
            val entry = openPosition.entry!!
            realized * growth(openPosition.getGrossProfit(closePrice) - entry.cost, openPosition)
        } else {
            realized
        }
        _points.add(EquityPoint(time, equity, openPosition.isOpened))
    }

    private fun growth(profit: Num, position: Position): Num {
        val entry = position.entry!!
        val invested = entry.pricePerAsset * entry.amount
        return invested.numFactory.one() + profit / invested
    }
}

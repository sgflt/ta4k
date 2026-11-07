/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.analysis

import java.time.Instant
import java.util.Collections
import java.util.NavigableMap
import java.util.TreeMap
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.Position.PositionValue
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Tracks the portfolio value evolution over time considering all price movements.
 */
class CashFlow {
    /** The portfolio value mapped to timestamps  */
    private val cashFlowValues = TreeMap<Instant, Num>()

    /** All cash flow values with their timestamps, read-only. */
    val values: NavigableMap<Instant, Num>
        get() = Collections.unmodifiableNavigableMap(cashFlowValues)


    /**
     * Constructor for cash flows of a single position.
     *
     * @param position a single position
     */
    constructor(position: Position) {
        if (position.entry != null) {
            calculatePositionValues(position)
        }
    }


    /**
     * Constructor for cash flows of a trading record.
     *
     * @param tradingRecord the trading record
     */
    constructor(tradingRecord: TradingRecord) {
        tradingRecord.positions.filter { it.entry != null }.forEach(::calculatePositionValues)

        // Calculate for current open position
        val currentPosition = tradingRecord.currentPosition
        if (currentPosition.isOpened) {
            calculatePositionValues(currentPosition)
        }
    }



    /**
     * Gets the cash flow value at a specific instant.
     *
     * @param instant the point in time
     * @return the cash flow value (returns 1 if no value exists for the instant)
     */
    fun getValue(instant: Instant): Num {
        return cashFlowValues.getOrDefault(instant, defaultNumFactory.one())
    }


    private fun calculatePositionValues(position: Position) {
        position.cashFlow.forEach { positionValue ->
            cashFlowValues.compute(positionValue.`when`) { _, v -> sumUpMultiplePositions(positionValue, v) }
        }
    }


    companion object {
        private fun sumUpMultiplePositions(positionValue: PositionValue, oldValue: Num?): Num? {
            if (oldValue == null) {
                return positionValue.value
            }

            return oldValue + positionValue.value
        }
    }
}

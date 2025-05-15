/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.analysis

import java.time.Instant
import java.util.*
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
    val values = sortedMapOf<Instant, Num>()


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
        tradingRecord.positions.stream()
            .filter { it.entry != null }
            .forEach { calculatePositionValues(it) }

        // Calculate for current open position
        val currentPosition = tradingRecord.currentPosition
        if (currentPosition.isOpened) {
            calculatePositionValues(currentPosition)
        }
    }


    /**
     * @return all cash flow values with their timestamps
     */
    fun getValues(): NavigableMap<Instant?, Num?> {
        return TreeMap<Instant?, Num?>(this.values)
    }


    /**
     * Gets the cash flow value at a specific instant.
     *
     * @param instant the point in time
     * @return the cash flow value (returns 1 if no value exists for the instant)
     */
    fun getValue(instant: Instant): Num {
        return this.values.getOrDefault(instant, defaultNumFactory.one())
    }


    private fun calculatePositionValues(position: Position) {
        position.cashFlow.forEach { positionValue ->
            this.values.compute(positionValue.`when`) { _, v -> sumUpMultiplePositions(positionValue, v) }
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

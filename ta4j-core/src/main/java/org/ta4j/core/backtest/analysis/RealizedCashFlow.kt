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

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.Trade
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import java.time.Instant
import java.util.*

/**
 * Tracks the money cash flow involved by a list of positions over time.
 *
 * buy at 100
 * price drops to 50
 * sell at 150
 *
 * flow is net +50
 *
 * This implementation does not track price fluctuations between entry and exit.
 */
class RealizedCashFlow {
    /** The cash flow values mapped to timestamps  */
    private val values: NavigableMap<Instant, Num>

    /** The num factory for creating numbers  */
    private val numFactory: NumFactory


    /**
     * Constructor for cash flows of a closed position.
     *
     * @param numFactory the number factory to use
     * @param position a single position
     */
    constructor(numFactory: NumFactory, position: Position) {
        this.numFactory = numFactory
        values = TreeMap<Instant, Num>()

        // Initialize with base value 1
        if (position.entry != null) {
            values.put(position.entry!!.whenExecuted, numFactory.one())
        }

        calculate(position)
    }


    /**
     * Constructor for cash flows of closed positions of a trading record.
     *
     * @param numFactory the number factory to use
     * @param tradingRecord the trading record
     */
    constructor(numFactory: NumFactory, tradingRecord: TradingRecord) {
        this.numFactory = numFactory
        values = TreeMap<Instant, Num>()

        // Initialize with base value 1 at first trade
        if (tradingRecord.positions.isNotEmpty()) {
            val firstTrade: Trade? = tradingRecord.positions.first().entry
            if (firstTrade != null) {
                values.put(firstTrade.whenExecuted, numFactory.one())
            }
        }

        calculate(tradingRecord)
    }


    /**
     * @return all cash flow values with their timestamps
     */
    fun getValues(): NavigableMap<Instant?, Num?> {
        return TreeMap<Instant?, Num?>(values)
    }


    /**
     * Calculates the cash flow for a single position.
     *
     * @param position a single position
     */
    private fun calculate(position: Position) {
        if (!position.isOpened) {
            calculateClosedPosition(position)
        } else {
            calculateOpenPosition(position, position.entry!!.whenExecuted)
        }
    }


    /**
     * Gets the cash flow value at a specific instant. If no value exists at that instant,
     * calculates the interpolated value based on the position state at that time.
     *
     * @param instant the point in time
     *
     * @return the cash flow value
     */
    fun getValue(instant: Instant): Num {
        // If we have a value at this exact instant, return it
        if (values.containsKey(instant)) {
            return values[instant]!!
        }

        // Get the last entry before this instant
        val lastEntry = values.floorEntry(instant)
        if (lastEntry == null) {
            return numFactory.one()
        }

        // Get the next entry after this instant
        val nextEntry = values.ceilingEntry(instant)
        if (nextEntry == null) {
            return lastEntry.value!!
        }

        // Linear interpolation between the two points
        val lastTime: Instant = lastEntry.key!!
        val nextTime: Instant = nextEntry.key!!
        val timeRange = nextTime.epochSecond - lastTime.epochSecond
        val currentTimeOffset = instant.epochSecond - lastTime.epochSecond

        // Calculate progress between the two points (0 to 1)
        val progress = numFactory.numOf(currentTimeOffset.toDouble() / timeRange)

        // Interpolate between the two values
        val valueRange = nextEntry.value!!.minus(lastEntry.value)
        return lastEntry.value!!.plus(valueRange.multipliedBy(progress))
    }


    private fun calculateClosedPosition(position: Position) {
        val isLongTrade = position.entry!!.isBuy
        val entry = position.entry
        val exit = position.exit

        // Calculate ratio
        val ratio = calculateRatio(isLongTrade, entry!!.netPrice, exit!!.netPrice)

        // Get the value at entry and multiply by ratio for exit value
        val entryValue = getValue(entry.whenExecuted)
        values.put(exit.whenExecuted, entryValue.multipliedBy(ratio))
    }


    private fun calculateOpenPosition(position: Position, evaluationTime: Instant?) {
        val isLongTrade = position.entry!!.isBuy
        val entry = position.entry
        val entryTime = entry!!.whenExecuted

        // Calculate holding costs
        val holdingCost = position.holdingCost
        val entryPrice = entry.netPrice

        // Record initial value
        val entryValue = getValue(entryTime)
        values.put(entryTime, entryValue)

        // Record value at evaluation time with holding costs
        val currentPrice = entry.pricePerAsset
        val adjustedPrice: Num = addCost(currentPrice, holdingCost, isLongTrade)
        val ratio = calculateRatio(isLongTrade, entryPrice, adjustedPrice)
        values.put(evaluationTime, entryValue.multipliedBy(ratio))
    }


    private fun calculate(tradingRecord: TradingRecord) {
        // Calculate for all closed positions
        tradingRecord.positions.forEach(this::calculate)

        // Calculate for current open position if any
        val currentPosition = tradingRecord.currentPosition
        if (currentPosition.isOpened) {
            // Use the latest timestamp from the trading record
            val latestTime =
                tradingRecord.positions.asSequence()
                    .filter { it.isClosed }
                    .map { it.exit?.whenExecuted }
                    .filterNotNull()
                    .maxOrNull()
                    ?: currentPosition.entry?.whenExecuted

            calculateOpenPosition(currentPosition, latestTime)
        }
    }


    /**
     * Calculates the ratio between entry and exit prices accounting for trade direction.
     */
    private fun calculateRatio(isLongTrade: Boolean, entryPrice: Num, exitPrice: Num): Num {
        if (isLongTrade) {
            return exitPrice.dividedBy(entryPrice)
        }

        // For short positions, when price goes down we gain
        // If price drops from 100 to 90, we gain 0.1 (10%)
        // ratio should be 1 + (entry - exit)/entry
        return numFactory.one().plus(entryPrice.minus(exitPrice).dividedBy(entryPrice))
    }


    companion object {
        /**
         * Adjusts price to incorporate trading costs.
         */
        private fun addCost(rawPrice: Num, holdingCost: Num, isLongTrade: Boolean): Num {
            return if (isLongTrade) rawPrice.minus(holdingCost) else rawPrice.plus(holdingCost)
        }
    }
}

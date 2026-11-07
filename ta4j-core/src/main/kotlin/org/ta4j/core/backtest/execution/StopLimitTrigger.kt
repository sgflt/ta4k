/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.execution

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.ExecutionTrigger
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Stop-limit order placed at the signal bar's close.
 *
 * A buy triggers once a later bar trades at or above `close * (1 + stopTriggerRatio)` and fills
 * only at or below `close * (1 + limitOffsetRatio)`; a sell mirrors this below the close. The fill
 * price is the stop, or the open if the bar gapped through the stop but not past the limit.
 * Unfilled orders expire after [maxBarsToFill] bars. Orders fill completely or not at all.
 */
class StopLimitTrigger(
    private val side: TradeType,
    referencePrice: Num,
    stopTriggerRatio: Num,
    limitOffsetRatio: Num,
    private val maxBarsToFill: Int,
    private val createdAt: Instant,
) : ExecutionTrigger {

    private val stopPrice: Num
    private val limitPrice: Num
    private var barsSeen = 0

    init {
        require(maxBarsToFill >= 1) { "Orders must live at least one bar" }
        require(limitOffsetRatio >= stopTriggerRatio) { "Limit offset must not be smaller than stop trigger" }
        val one = referencePrice.numFactory.one()
        if (side == TradeType.BUY) {
            stopPrice = referencePrice * (one + stopTriggerRatio)
            limitPrice = referencePrice * (one + limitOffsetRatio)
        } else {
            stopPrice = referencePrice * (one - stopTriggerRatio)
            limitPrice = referencePrice * (one - limitOffsetRatio)
        }
    }

    override fun shouldExecute(event: MarketEvent): Boolean {
        if (event !is CandleReceived || !event.beginTime.isAfter(createdAt)) {
            return false
        }
        ++barsSeen
        val high = stopPrice.numFactory.numOf(event.highPrice)
        val low = stopPrice.numFactory.numOf(event.lowPrice)
        return if (side == TradeType.BUY) {
            high >= stopPrice && low <= limitPrice
        } else {
            low <= stopPrice && high >= limitPrice
        }
    }

    override fun getExecutionPrice(event: MarketEvent, numFactory: NumFactory): Num? {
        if (event !is CandleReceived) {
            return null
        }
        val open = numFactory.numOf(event.openPrice)
        return if (side == TradeType.BUY) {
            minOf(maxOf(open, stopPrice), limitPrice)
        } else {
            maxOf(minOf(open, stopPrice), limitPrice)
        }
    }

    override fun isExpired(event: MarketEvent): Boolean = barsSeen >= maxBarsToFill
}

/**
 * Places every strategy signal as a [StopLimitTrigger].
 *
 * @param stopTriggerRatio e.g. 0.01 to trigger 1 % beyond the signal close
 * @param limitOffsetRatio e.g. 0.02 to accept fills up to 2 % beyond the signal close
 */
class StopLimitOrders(
    private val stopTriggerRatio: Number,
    private val limitOffsetRatio: Number,
    private val maxBarsToFill: Int = 1,
) : OrderTriggerFactory {
    override fun create(side: TradeType, event: MarketEvent, numFactory: NumFactory): ExecutionTrigger {
        val close = (event as? CandleReceived)?.closePrice
            ?: error("Stop-limit orders need candle events")
        return StopLimitTrigger(
            side,
            numFactory.numOf(close),
            numFactory.numOf(stopTriggerRatio),
            numFactory.numOf(limitOffsetRatio),
            maxBarsToFill,
            event.beginTime
        )
    }
}

/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.api.series.Bar
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.strategy.BackTestTradingRecord.TradingRecordContextKeys
import org.ta4j.core.events.TickReceived
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * Runtime context publishing a single position the same way the backtest does.
 */
class PositionRuntimeContext(var position: Position?) : RuntimeContext {

    private var tradeTime = Instant.EPOCH

    /**
     * Opens a new position at [price], replacing the current one.
     */
    fun enter(numFactory: NumFactory, price: Double, tradeType: TradeType = TradeType.BUY) {
        position = Position(startingType = tradeType, numFactory = numFactory).apply {
            operate(nextTradeTime(), numFactory.numOf(price), numFactory.one())
        }
    }

    /**
     * Closes the current position at [price].
     */
    fun exit(price: Double) {
        val current = checkNotNull(position) { "No position to exit" }
        val numFactory = current.entry!!.pricePerAsset.numFactory
        current.operate(nextTradeTime(), numFactory.numOf(price), numFactory.one())
    }

    private fun nextTradeTime(): Instant {
        tradeTime = tradeTime.plusSeconds(60)
        return tradeTime
    }

    override fun <T> getValue(resolver: RuntimeValueResolver<T?>): T? = resolver.resolve(this)

    override fun getValue(key: String): Any? =
        if (key == TradingRecordContextKeys.CURRENT_POSITION) position else null

    override fun onBar(bar: Bar) {
        // static fixture
    }

    override fun onTick(tick: TickReceived) {
        // static fixture
    }

    companion object {
        /**
         * @param entryPrice entry price of an open position, or null for no open position
         */
        fun withOpenPosition(
            numFactory: NumFactory,
            entryPrice: Double?,
            tradeType: TradeType = TradeType.BUY,
        ): RuntimeContext {
            val position = entryPrice?.let {
                Position(startingType = tradeType, numFactory = numFactory).apply {
                    operate(Instant.EPOCH, numFactory.numOf(it), numFactory.one())
                }
            }
            return PositionRuntimeContext(position)
        }
    }
}

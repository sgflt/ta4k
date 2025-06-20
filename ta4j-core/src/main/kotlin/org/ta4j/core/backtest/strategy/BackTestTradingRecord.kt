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
package org.ta4j.core.backtest.strategy

import java.time.Instant
import java.util.*
import lombok.experimental.UtilityClass
import org.ta4j.core.TradeType
import org.ta4j.core.api.series.Bar
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.Trade
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.backtest.analysis.cost.ZeroCostModel
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextKeys
import org.ta4j.core.events.TickReceived
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

/**
 * Base implementation of a [TradingRecord].
 */
open class BackTestTradingRecord @JvmOverloads constructor(
    override val startingType: TradeType,
    override var name: String = "",
    /** The cost model for transactions of the asset.  */
    private val transactionCostModel: CostModel = ZeroCostModel,
    /** The cost model for holding asset (e.g. borrowing).  */
    private val holdingCostModel: CostModel = ZeroCostModel,
    private val numFactory: NumFactory,
) : TradingRecord, RuntimeContext {
    /** The recorded trades.  */
    private val trades = mutableListOf<Trade>()

    /** The recorded positions.  */
    override val positions = mutableListOf<Position>()

    private var _currentPosition: Position

    /** The current non-closed position (there's always one).  */
    override val currentPosition
        get() = _currentPosition

    private var currentTime = Instant.MIN
    private var currentPrice = numFactory.zero()


    /**
     * @param entryTradeType the [trade type][TradeType] of entries in
     * the trading session
     * @param transactionCostModel the cost model for transactions of the asset
     * @param holdingCostModel the cost model for holding the asset (e.g.
     * borrowing)
     *
     * @throws NullPointerException if entryTradeType is null
     */
    init {
        _currentPosition = Position(
            startingType,
            transactionCostModel,
            holdingCostModel,
            numFactory
        )
    }


    override val maximumDrawdown
        get() = positions.maxOfOrNull<Position, Num> { it.maxDrawdown } ?: numFactory.zero()

    private fun operate(whenExecuted: Instant, pricePerAsset: Num, amount: Num) {
        check(!this.currentPosition.isClosed) { "Current position should not be closed" }

        val newTrade = this.currentPosition.operate(whenExecuted, pricePerAsset, amount)
        recordTrade(newTrade)
    }


    override fun enter(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Boolean {
        if (this.currentPosition.isNew) {
            operate(whenExecuted, pricePerAsset, amount)
            return true
        }

        return false
    }


    override fun exit(whenExecuted: Instant, pricePerAsset: Num, amount: Num): Boolean {
        if (this.currentPosition.isOpened) {
            operate(whenExecuted, pricePerAsset, amount)
            return true
        }

        return false
    }


    override val lastTrade
        get() = trades.lastOrNull()


    override fun getLastTrade(tradeType: TradeType?): Trade? {
        return this.trades
            .asSequence()
            .filter { it.type == tradeType }
            .maxByOrNull { it.whenExecuted }
    }


    override val lastEntry
        get() = trades
            .asSequence()
            .filter { it.orderType == Trade.OrderType.OPEN }
            .maxByOrNull { it.whenExecuted }


    override val lastExit
        get() = trades
            .asSequence()
            .filter { it.orderType == Trade.OrderType.CLOSE }
            .maxByOrNull { it.whenExecuted }


    override val isEmpty
        get() = trades.isEmpty()


    /**
     * Records a trade and the corresponding position (if closed).
     *
     * @param trade the trade to be recorded
     *
     * @throws NullPointerException if trade is null
     */
    private fun recordTrade(trade: Trade?) {
        Objects.requireNonNull<Trade?>(trade, "Trade should not be null")

        // Storing the new trade in trades list
        this.trades.add(trade!!)

        // Storing the position if closed
        if (currentPosition.isClosed == true) {
            positions.add(currentPosition)
            _currentPosition = Position(startingType, transactionCostModel, holdingCostModel, numFactory)
        }
    }


    override fun toString(): String {
        val sb = StringBuilder().append("BackTestTradingRecord: ")
            .append(this.name)
            .append(System.lineSeparator())
        for (trade in this.trades) {
            sb.append(trade.toString()).append(System.lineSeparator())
        }
        return sb.toString()
    }


    override fun onBar(bar: Bar) {
        this.currentPosition.onBar(bar) // TODO allow DCA

        this.currentTime = bar.endTime
        this.currentPrice = bar.closePrice
    }


    override fun onTick(tick: TickReceived) {
        this.currentTime = tick.beginTime
        this.currentPrice = this.numFactory.numOf((tick.ask + tick.bid) / 2.0)
    }


    override fun <T> getValue(resolver: RuntimeValueResolver<T?>): T? {
        return resolver.resolve(this)
    }


    override fun getValue(key: String): Any? {
        return when (key) {
            TradingRecordContextKeys.CURRENT_POSITION -> this.currentPosition
            TradingRecordContextKeys.BACKTEST_TRADING_RECORD -> this
            RuntimeContextKeys.CURRENT_PRICE -> this.currentPrice
            RuntimeContextKeys.CURRENT_TIME -> this.currentTime
            else -> null
        }
    }


    @UtilityClass
    object TradingRecordContextKeys {
        const val CURRENT_POSITION: String = "ta4j-currentPosition"
        const val BACKTEST_TRADING_RECORD: String = "ta4j-backtestTradingRecord"
    }


    class CurrentPositionResolver : RuntimeValueResolver<Position?> {
        override fun resolve(context: RuntimeContext): Position? {
            return context.getValue(TradingRecordContextKeys.CURRENT_POSITION) as Position?
        }
    }

    class TradeRecordResolver : RuntimeValueResolver<BackTestTradingRecord?> {
        override fun resolve(context: RuntimeContext): BackTestTradingRecord? {
            return context.getValue(TradingRecordContextKeys.BACKTEST_TRADING_RECORD) as BackTestTradingRecord?
        }
    }
}

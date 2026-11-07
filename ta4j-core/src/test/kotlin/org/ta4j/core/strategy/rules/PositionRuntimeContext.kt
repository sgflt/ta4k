/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective
 * authors (see AUTHORS)
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
class PositionRuntimeContext(private val position: Position?) : RuntimeContext {

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

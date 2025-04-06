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
package org.ta4j.core.backtest.strategy

import lombok.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.callback.TickListener
import org.ta4j.core.api.series.Bar
import org.ta4j.core.backtest.OperationType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.strategy.BackTestTradingRecord.CurrentPositionResolver
import org.ta4j.core.backtest.strategy.BackTestTradingRecord.TradeRecordResolver
import org.ta4j.core.events.TickReceived
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import java.time.Instant

/**
 * This implementation is designed for backtesting of custom strategy.
 *
 * It adds trading record for performance analysis.
 *
 * Tested strategy is wrapped into this adaptation class.
 */
@ToString
class BacktestStrategy(
    private val testedStrategy: Strategy,
    private val runtimeContext: RuntimeContext,
) : Strategy, BarListener, TickListener {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var currentTime: Instant

    override val name: String
        get() = testedStrategy.name

    override val timeFrames: Set<TimeFrame>
        get() = testedStrategy.timeFrames

    override val entryRule: Rule
        get() = testedStrategy.entryRule

    override val exitRule: Rule
        get() = testedStrategy.exitRule

    override val isStable: Boolean
        get() = testedStrategy.isStable


    /**
     * @return [OperationType.ENTER] to recommend entering, [OperationType.EXIT] to recommend exit, otherwise
     * (no recommendation)
     */
    fun shouldOperate(): OperationType {
        val position = runtimeContext.getValue<Position>(CurrentPositionResolver())!!
        if (position.isNew) {
            if (shouldEnter()) {
                return OperationType.ENTER
            }
        } else if (position.isOpened && shouldExit()) {
            return OperationType.EXIT
        }
        return OperationType.NOOP
    }


    override fun shouldEnter(): Boolean {
        val enter = testedStrategy.shouldEnter()
        traceShouldEnter(enter)
        return enter
    }


    override fun shouldExit(): Boolean {
        val exit = testedStrategy.shouldExit()
        traceShouldExit(exit)
        return exit
    }


    override fun onBar(bar: Bar) {
        currentTime = bar.endTime
    }


    override fun onTick(tick: TickReceived) {
        currentTime = tick.beginTime
    }


    /**
     * Traces the `shouldEnter()` method calls.
     *
     * @param enter true if the strategy should enter, false otherwise
     */
    private fun traceShouldEnter(enter: Boolean) {
        if (log.isTraceEnabled) {
            log.trace(">>> {}#shouldEnter({}): {}", name, currentTime, enter)
        }
    }


    /**
     * Traces the `shouldExit()` method calls.
     *
     * @param exit true if the strategy should exit, false otherwise
     */
    private fun traceShouldExit(exit: Boolean) {
        if (log.isTraceEnabled) {
            log.trace(">>> {}#shouldExit({}): {}", name, currentTime, exit)
        }
    }


    val tradeRecord: BackTestTradingRecord
        get() = runtimeContext.getValue<BackTestTradingRecord>(TradeRecordResolver())!!
}

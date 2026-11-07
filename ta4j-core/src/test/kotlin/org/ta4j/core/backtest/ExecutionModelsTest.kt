/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.execution.PositionSizer
import org.ta4j.core.backtest.execution.SlippageModel
import org.ta4j.core.backtest.execution.StopLimitOrders
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter.convert
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContextFactory
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.strategy.rules.BooleanRule

internal class ExecutionModelsTest {

    private var day = 0L

    private fun candle(open: Double, high: Double, low: Double, close: Double): CandleReceived {
        val begin = Instant.EPOCH.plus(Duration.ofDays(day++))
        return CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = open,
            highPrice = high,
            lowPrice = low,
            closePrice = close,
            volume = 1000.0
        )
    }

    private fun candle(close: Double) = candle(close, close, close, close)

    private fun record(numFactory: NumFactory) =
        BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory)

    private fun currentClose(record: TradingRecord, numFactory: NumFactory, slippage: SlippageModel, sizer: PositionSizer? = null) =
        PendingOrderManager(record, numFactory, OrderTriggerFactoryOfCurrentClose, slippage, sizer)

    private val OrderTriggerFactoryOfCurrentClose =
        org.ta4j.core.backtest.execution.OrderTriggerFactory.of(ExecutionMode.CURRENT_CLOSE)

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun slippageWorsensBuysAndSells(numFactory: NumFactory) {
        val record = record(numFactory)
        val manager = currentClose(record, numFactory, SlippageModel.percentage(1))

        candle(100.0).let { manager.createOrder(OperationType.ENTER, it, numFactory.one()); manager.processPendingOrders(it) }
        candle(110.0).let { manager.createOrder(OperationType.EXIT, it, numFactory.one()); manager.processPendingOrders(it) }

        val position = record.lastPosition!!
        assertNumEquals(101.0, position.entry!!.pricePerAsset)
        assertNumEquals(108.9, position.exit!!.pricePerAsset)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun fixedSlippageForShortEntry(numFactory: NumFactory) {
        val record = BackTestTradingRecord(startingType = TradeType.SELL, numFactory = numFactory)
        val manager = currentClose(record, numFactory, SlippageModel.fixed(0.5))
        candle(100.0).let { manager.createOrder(OperationType.ENTER, it, numFactory.one()); manager.processPendingOrders(it) }
        assertNumEquals(99.5, record.currentPosition.entry!!.pricePerAsset)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun sizerSetsEntryAmountAndExitClosesIt(numFactory: NumFactory) {
        val record = record(numFactory)
        val manager = currentClose(record, numFactory, SlippageModel.NONE, PositionSizer.fixedCash(1000))

        candle(100.0).let { manager.createOrder(OperationType.ENTER, it, numFactory.one()); manager.processPendingOrders(it) }
        candle(125.0).let { manager.createOrder(OperationType.EXIT, it, numFactory.one()); manager.processPendingOrders(it) }

        val position = record.lastPosition!!
        assertNumEquals(10.0, position.entry!!.amount)
        assertNumEquals(10.0, position.exit!!.amount)
        assertNumEquals(250.0, position.profit)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun fractionOfEquityUsesRealizedProfit(numFactory: NumFactory) {
        val record = record(numFactory)
        val sizer = PositionSizer.fractionOfEquity(10_000, 0.5)
        assertNumEquals(50.0, sizer.size(numFactory.hundred(), record))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun duplicateSignalsDoNotQueueTwice(numFactory: NumFactory) {
        val manager = PendingOrderManager(record(numFactory), ExecutionMode.NEXT_OPEN, numFactory)
        val signal = candle(100.0)
        manager.createOrder(OperationType.ENTER, signal, numFactory.one())
        manager.createOrder(OperationType.ENTER, signal, numFactory.one())
        assertThat(manager.getPendingOrderCount()).isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stopLimitBuyFillsAtStopWhenTriggered(numFactory: NumFactory) {
        val record = record(numFactory)
        // stop 101, limit 102
        val manager = PendingOrderManager(record, numFactory, StopLimitOrders(0.01, 0.02, maxBarsToFill = 2))
        val signal = candle(100.0)
        manager.createOrder(OperationType.ENTER, signal, numFactory.one())
        manager.processPendingOrders(signal)
        assertThat(manager.getPendingOrderCount()).isEqualTo(1) // never on the signal bar

        manager.processPendingOrders(candle(100.0, 100.5, 99.0, 100.2)) // stop not reached
        assertThat(record.currentPosition.isNew).isTrue()

        manager.processPendingOrders(candle(100.5, 103.0, 100.0, 102.5))
        assertNumEquals(101.0, record.currentPosition.entry!!.pricePerAsset)
        assertThat(manager.getPendingOrderCount()).isZero()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stopLimitBuyFillsAtOpenOnGapWithinLimit(numFactory: NumFactory) {
        val record = record(numFactory)
        val manager = PendingOrderManager(record, numFactory, StopLimitOrders(0.01, 0.02))
        val signal = candle(100.0)
        manager.createOrder(OperationType.ENTER, signal, numFactory.one())
        manager.processPendingOrders(candle(101.5, 103.0, 101.2, 102.0))
        assertNumEquals(101.5, record.currentPosition.entry!!.pricePerAsset)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun stopLimitExpiresWhenGapSkipsLimit(numFactory: NumFactory) {
        val record = record(numFactory)
        val manager = PendingOrderManager(record, numFactory, StopLimitOrders(0.01, 0.02, maxBarsToFill = 1))
        manager.createOrder(OperationType.ENTER, candle(100.0), numFactory.one())
        manager.processPendingOrders(candle(103.0, 104.0, 102.5, 103.5)) // never trades at or below 102
        assertThat(record.currentPosition.isNew).isTrue()
        assertThat(manager.getPendingOrderCount()).isZero()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun closeOpenPositionAtEndExitsAtLastClose(numFactory: NumFactory) {
        val events = listOf(candle(100.0), candle(105.0), candle(120.0))
        val statement = BacktestExecutorBuilder()
            .numFactory(numFactory)
            .closeOpenPositionAtEnd()
            .build()
            .execute(AlwaysInRun(), events, 1.0)

        val record = statement.strategy.tradeRecord
        assertThat(record.currentPosition.isNew).isTrue()
        assertThat(record.positionCount).isEqualTo(1)
        assertNumEquals(20.0, record.lastPosition!!.profit)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun positionStaysOpenWithoutCloseAtEnd(numFactory: NumFactory) {
        val events = listOf(candle(100.0), candle(105.0), candle(120.0))
        val statement = BacktestExecutorBuilder().numFactory(numFactory).build().execute(AlwaysInRun(), events, 1.0)
        assertThat(statement.strategy.tradeRecord.currentPosition.isOpened).isTrue()
    }

    /** Enters on the first bar and never exits. */
    private class AlwaysInRun : BacktestRun {
        override val runtimeContextFactory: RuntimeContextFactory = NOOPRuntimeContextFactory()
        override val configuration = StrategyConfiguration()
        override val strategyFactory: StrategyFactory<BacktestStrategy> = convert(object : StrategyFactory<Strategy> {
            override val tradeType = TradeType.BUY
            override fun createStrategy(
                configuration: StrategyConfiguration,
                runtimeContext: RuntimeContext,
                indicatorContexts: IndicatorContexts,
            ): Strategy = DefaultStrategy(
                name = "always-in",
                timeFrames = setOf(TimeFrame.DAY),
                entryRule = BooleanRule.TRUE,
                exitRule = BooleanRule.FALSE,
                indicatorContexts = indicatorContexts
            )
        })
    }
}

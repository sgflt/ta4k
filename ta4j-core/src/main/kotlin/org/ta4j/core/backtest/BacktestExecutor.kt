/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import org.ta4j.core.MultiTimeFrameSeries
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.api.callback.TickListener
import org.ta4j.core.backtest.reports.TradingStatement
import org.ta4j.core.backtest.reports.TradingStatementGenerator
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.events.NewsReceived
import org.ta4j.core.events.TickReceived
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.num.Num
import org.ta4j.core.strategy.CompoundRuntimeContext
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.StrategyFactory

/**
 * Allows backtesting multiple strategies and comparing them to find out which
 * is the best.
 */
class BacktestExecutor internal constructor(private val configuration: BacktestConfiguration) {
    /**
     * Executes given strategies with specified trade type to open the position and
     * return the trading statements.
     *
     * @param backtestRun that creates strategies to test and their contexts
     * @param amount the amount used to open/close the position
     *
     * @return a list of TradingStatements
     */
    fun execute(
        backtestRun: BacktestRun,
        marketEvents: List<MarketEvent>,
        amount: Number,
    ): TradingStatement {
        val marketEventHandler = DefaultMarketEventHandler(backtestRun, configuration.numFactory.numOf(amount))
        replay(
            marketEvents,
            marketEventHandler
        )
        if (configuration.closeOpenPositionAtEnd) {
            marketEventHandler.closeOpenPosition(marketEvents)
        }
        return marketEventHandler.tradingStatement
    }


    private fun replay(
        marketEvents: List<MarketEvent>,
        marketEventHandler: MarketEventHandler,
    ) {
        marketEvents
            .sortedBy { it.beginTime }
            .forEach { marketEvent ->
                when (marketEvent) {
                    is CandleReceived -> marketEventHandler.onCandle(marketEvent)
                    is NewsReceived -> marketEventHandler.onNews(marketEvent)
                    is TickReceived -> marketEventHandler.onTick(marketEvent)
                    else -> error("Unexpected value: $marketEvent")
                }
            }
    }


    /**
     * Context links Strategy, TradingRecord, IndicatorContext and BarSeries.
     */
    private inner class DefaultMarketEventHandler(
        backtestRun: BacktestRun,
        private val amount: Num,
    ) : MarketEventHandler {
        private val multiTimeFrameSeries = MultiTimeFrameSeries<BacktestBarSeries>()
        private val tickListeners = mutableListOf<TickListener>()
        private val strategy: BacktestStrategy = createStrategy(backtestRun)
        private lateinit var runtimeContext: RuntimeContext
        private lateinit var pendingOrderManager: PendingOrderManager

        private fun createStrategy(backtestRun: BacktestRun): BacktestStrategy {
            val strategyFactory = backtestRun.strategyFactory

            val runtimeContext = getCompoundRuntimeContext(backtestRun, strategyFactory)
            val indicatorContexts = IndicatorContexts.empty()

            val strategy = strategyFactory.createStrategy(
                backtestRun.configuration,
                runtimeContext,
                indicatorContexts
            )

            strategy.timeFrames.forEach { timeFrame ->
                val series = BacktestBarSeriesBuilder()
                    .withIndicatorContext(indicatorContexts[timeFrame])
                    .withNumFactory(this@BacktestExecutor.configuration.numFactory)
                    .build().apply {
                        addBarListener(strategy)
                        addBarListener(runtimeContext)
                    }
                multiTimeFrameSeries.add(series)
            }

            this.runtimeContext = runtimeContext
            this.pendingOrderManager = PendingOrderManager(
                tradingRecord = strategy.tradeRecord,
                numFactory = configuration.numFactory,
                orderTriggerFactory = configuration.orderTriggerFactory,
                slippageModel = configuration.slippageModel,
                positionSizer = configuration.positionSizer,
            )
            tickListeners.add(strategy)
            tickListeners.add(runtimeContext)
            return strategy
        }

        private fun getCompoundRuntimeContext(
            backtestRun: BacktestRun,
            strategyFactory: StrategyFactory<BacktestStrategy>,
        ): CompoundRuntimeContext {
            val backTestTradingRecord = BackTestTradingRecord(
                startingType = strategyFactory.tradeType,
                transactionCostModel = configuration.transactionCostModel,
                holdingCostModel = configuration.holdingCostModel,
                numFactory = configuration.numFactory,
            )

            return CompoundRuntimeContext.of(
                backtestRun.runtimeContextFactory.createRuntimeContext(),
                backTestTradingRecord
            )
        }


        override fun onCandle(event: CandleReceived) {
            multiTimeFrameSeries.onCandle(event)

            // Process pending orders first (for NEXT_OPEN mode)
            pendingOrderManager.processPendingOrders(event)

            // Evaluate strategy for new signals
            evaluateStrategy(event)

            // Process pending orders again (for CURRENT_CLOSE mode - triggers immediately)
            pendingOrderManager.processPendingOrders(event)
        }

        private fun evaluateStrategy(event: MarketEvent) {
            when (strategy.shouldOperate()) {
                OperationType.ENTER -> pendingOrderManager.createOrder(
                    OperationType.ENTER,
                    event,
                    amount
                )

                OperationType.EXIT -> pendingOrderManager.createOrder(
                    OperationType.EXIT,
                    event,
                    amount
                )

                OperationType.NOOP -> Unit
            }
        }

        override fun onTick(event: TickReceived) {
            tickListeners.forEach { it.onTick(event) }

            // Process pending orders first (for NEXT_OPEN mode)
            pendingOrderManager.processPendingOrders(event)

            // Evaluate strategy for new signals
            evaluateStrategy(event)

            // Process pending orders again (for CURRENT_CLOSE mode - triggers immediately)
            pendingOrderManager.processPendingOrders(event)
        }


        override fun onNews(event: NewsReceived) {
            // TODO
        }


        /** Exits a position left open at the close of the last candle, with slippage applied. */
        fun closeOpenPosition(marketEvents: List<MarketEvent>) {
            val record = strategy.tradeRecord
            val position = record.currentPosition
            if (!position.isOpened) {
                return
            }
            val lastCandle = marketEvents.filterIsInstance<CandleReceived>().maxByOrNull { it.beginTime } ?: return
            val close = configuration.numFactory.numOf(lastCandle.closePrice)
            val price = configuration.slippageModel.apply(record.startingType.complementType(), close)
            record.exit(lastCandle.endTime, price, position.entry!!.amount)
        }

        val tradingStatement
            get() = TradingStatementGenerator().generate(strategy)
    }
}

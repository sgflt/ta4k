/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Duration
import java.time.Instant
import java.util.Random
import kotlin.math.max
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TestUtils.assertNumEquals
import org.ta4j.core.TradeType
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.backtest.strategy.BacktestRun
import org.ta4j.core.backtest.strategy.BacktestStrategy
import org.ta4j.core.backtest.strategy.StrategyFactoryConverter.convert
import org.ta4j.core.backtest.strategy.runtime.NOOPRuntimeContextFactory
import org.ta4j.core.backtest.strategy.runtime.RuntimeContextFactory
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.MarketEvent
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.Rule
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.ParameterName
import org.ta4j.core.strategy.configuration.StrategyConfiguration

internal class BacktestExecutorTest {
    private val time: Instant = Instant.EPOCH
    lateinit var random: Random


    @BeforeEach
    fun setUp() {
        this.random = Random(0L)
    }


    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun execute(numFactory: NumFactory) {
        val executor = BacktestExecutorBuilder()
            .numFactory(numFactory)
            .build()

        val statement = executor.execute(
            backtestRunFactory,
            marketEvents,
            1.0
        )

        log.info(statement.toString())
        assertNumEquals(-4.75019506722970, statement.performanceReport.totalLoss!!)
        assertNumEquals(718.71244689979, statement.performanceReport.totalProfit!!)
        assertNumEquals(713.96225183256, statement.performanceReport.totalProfitLoss!!)
        assertNumEquals(9.572571228801804, statement.performanceReport.totalProfitLossPercentage!!)
    }


    private val marketEvents: MutableList<MarketEvent>
        get() {
            val events = mutableListOf<MarketEvent>()
            var marketEvent = generateEvent()
            events.add(marketEvent)
            for (i in 0..9999) {
                marketEvent = generateEvent(marketEvent)
                events.add(marketEvent)
            }
            return events
        }


    private fun generateEvent(): CandleReceived = CandleReceived(
        timeFrame = TimeFrame.DAY,
        beginTime = this.time,
        endTime = this.time.plus(Duration.ofDays(1)),
        closePrice = 100.0
    )

    private fun generateEvent(previous: CandleReceived): CandleReceived = CandleReceived(
        timeFrame = previous.timeFrame,
        beginTime = previous.endTime,
        endTime = previous.endTime.plus(Duration.ofDays(1)),
        closePrice = max(0.0, previous.closePrice + this.random.nextDouble(-1.0, 1.2))
    )


    private class TestBacktestRun : BacktestRun {
        override val runtimeContextFactory: RuntimeContextFactory
            get() = NOOPRuntimeContextFactory()


        override val strategyFactory: StrategyFactory<BacktestStrategy>
            get() = convert(TestStrategyFactory())


        override val configuration: StrategyConfiguration
            get() {
                val configuration = StrategyConfiguration()
                configuration.put(ParameterName("smaFastBars"), 11)
                configuration.put(ParameterName("smaSlowBars"), 200)
                return configuration
            }
    }


    private class TestStrategyFactory : StrategyFactory<Strategy> {
        override val tradeType: TradeType
            get() = TradeType.BUY


        override fun createStrategy(
            configuration: StrategyConfiguration,
            runtimeContext: RuntimeContext,
            indicatorContexts: IndicatorContexts,
        ): Strategy {
            val closePrice = closePrice()
            val indicatorContext = indicatorContexts.get(TimeFrame.DAY)
            indicatorContext.add(
                closePrice.sma(configuration.getInt(ParameterName("smaFastBars")) ?: 11),
                SMA_FAST
            )
            indicatorContext.add(
                closePrice.sma(configuration.getInt(ParameterName("smaSlowBars")) ?: 200),
                SMA_SLOW
            )

            return DefaultStrategy(
                name = "test-strategy",
                timeFrames = setOf(TimeFrame.DAY),
                entryRule = createEntryRule(indicatorContext),
                exitRule = createExitRule(indicatorContext),
                indicatorContexts = indicatorContexts
            )
        }


        fun createEntryRule(indicatorContext: IndicatorContext): Rule {
            val smaFast = indicatorContext.getNumericIndicator(SMA_FAST)
            val crossIndicator = smaFast!!.crossedOver(indicatorContext.getNumericIndicator(SMA_SLOW)!!)
            indicatorContext.add(crossIndicator)
            return crossIndicator.toRule()
        }


        fun createExitRule(indicatorContext: IndicatorContext): Rule {
            val smaFast = indicatorContext.getNumericIndicator(SMA_FAST)
            val crossIndicator = smaFast!!.crossedUnder(indicatorContext.getNumericIndicator(SMA_SLOW)!!)
            indicatorContext.add(crossIndicator)
            return crossIndicator.toRule()
        }

        companion object {
            private val SMA_FAST = IndicatorIdentification("smaFast")
            private val SMA_SLOW = IndicatorIdentification("smaSlow")
        }
    }

    companion object {
        private val backtestRunFactory: TestBacktestRun
            get() = TestBacktestRun()

        val log = org.slf4j.LoggerFactory.getLogger(BacktestExecutorTest::class.java)
    }
}

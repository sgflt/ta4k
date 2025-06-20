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
package org.ta4j.core.trading.live

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.events.CandleReceived
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
import org.ta4j.core.strategy.configuration.StrategyConfiguration

class LiveTradingTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun testStandardFlow(numFactory: NumFactory) {
        // Create indicator contexts
        val indicatorContexts = IndicatorContexts.empty()

        // Create configuration
        val configuration = StrategyConfiguration()

        // Create live trading instance
        val liveTrading = LiveTradingBuilder()
            .withNumFactory(numFactory)
            .withName("LiveTrading")
            .withStrategyFactory(TestSMAStrategyFactory(numFactory))
            .withConfiguration(configuration)
            .withIndicatorContexts(indicatorContexts)
            .build()

        // Test initial state - no candles yet, so no entry/exit signals
        assertThat(liveTrading.shouldEnter()).isFalse()
        assertThat(liveTrading.shouldExit()).isFalse()

        val baseTime = Instant.now()
        val duration = Duration.ofMinutes(1)

        // Send 6 candles with close price 10
        // SMA(5) values should be: 2, 4, 6, 8, 10, 10
        for (i in 0 until 6) {
            val candle = CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = baseTime.plus(duration.multipliedBy(i.toLong())),
                endTime = baseTime.plus(duration.multipliedBy((i + 1).toLong())),
                openPrice = 10.0,
                closePrice = 10.0,
                highPrice = 10.0,
                lowPrice = 10.0,
                volume = 100.0
            )
            liveTrading.onCandle(candle)

            // Check SMA value and strategy signals
            when (i) {
                0, 1, 2, 3 -> {
                    // SMA < 5, so no entry signal
                    assertThat(liveTrading.shouldEnter()).isFalse()
                    assertThat(liveTrading.shouldExit()).isFalse()
                }

                4, 5 -> {
                    // SMA >= 5 and SMA < 11, so both entry and exit would be true
                    // But in a real strategy, you'd typically be either entering OR exiting
                    assertThat(liveTrading.shouldEnter()).isTrue()
                    assertThat(liveTrading.shouldExit()).isTrue()
                }
            }
        }

        // Send one more candle with higher close price (100)
        // This should make SMA = (10+10+10+10+100)/5 = 28
        val finalCandle = CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = baseTime.plus(duration.multipliedBy(6)),
            endTime = baseTime.plus(duration.multipliedBy(7)),
            openPrice = 100.0,
            closePrice = 100.0,
            highPrice = 100.0,
            lowPrice = 100.0,
            volume = 100.0
        )
        liveTrading.onCandle(finalCandle)

        // SMA = 28, which is > 11, so exit should be false, entry should be true
        assertThat(liveTrading.shouldEnter()).isTrue()
        assertThat(liveTrading.shouldExit()).isFalse()
    }

    /**
     * Test strategy factory that creates a simple SMA-based strategy
     * Entry rule: SMA(5) > 5
     * Exit rule: SMA(5) < 11
     */
    private class TestSMAStrategyFactory(private val numFactory: NumFactory) : StrategyFactory<Strategy> {
        override val tradeType: TradeType = TradeType.BUY

        override fun createStrategy(
            configuration: StrategyConfiguration,
            runtimeContext: RuntimeContext,
            indicatorContexts: IndicatorContexts,
        ): Strategy {
            val indicatorContext = indicatorContexts[TimeFrame.DAY]

            // Create close price indicator and SMA using the correct numFactory
            val closePrice = org.ta4j.core.api.Indicators.extended(numFactory).closePrice()
            val sma = closePrice.sma(5)

            // Add indicators to context with identifications
            indicatorContext.add(closePrice, CLOSE_PRICE)
            indicatorContext.add(sma, SMA_5)

            return DefaultStrategy(
                name = "LiveSMA",
                timeFrames = setOf(TimeFrame.DAY),
                entryRule = createEntryRule(indicatorContext),
                exitRule = createExitRule(indicatorContext),
                indicatorContext = indicatorContext
            )
        }

        private fun createEntryRule(indicatorContext: IndicatorContext): Rule {
            val sma = indicatorContext.getNumericIndicator(SMA_5)!!
            return sma.isGreaterThanRule(5)
        }

        private fun createExitRule(indicatorContext: IndicatorContext): Rule {
            val sma = indicatorContext.getNumericIndicator(SMA_5)!!
            return sma.isLessThanRule(11)
        }

        companion object {
            private val CLOSE_PRICE = IndicatorIdentification("closePrice")
            private val SMA_5 = IndicatorIdentification("sma5")
        }
    }
}

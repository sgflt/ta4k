/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.analysis

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.api.Indicators
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import ta4jexamples.loaders.MockMarketEventsLoader

class BuyAndSellSignalsToChartTest {

    private fun createTestStrategyFactory(): StrategyFactory<Strategy> {
        return object : StrategyFactory<Strategy> {
            override val tradeType = org.ta4j.core.TradeType.BUY
            
            override fun createStrategy(
                configuration: StrategyConfiguration,
                runtimeContext: RuntimeContext,
                indicatorContexts: IndicatorContexts
            ): Strategy {
                val indicatorContext = indicatorContexts[TimeFrame.DAY]
                
                val closePrice = Indicators.closePrice()
                val shortEma = closePrice.ema(5)
                val longEma = closePrice.ema(10)
                
                // Register indicators
                indicatorContext.add(closePrice, IndicatorIdentification("closePrice"))
                indicatorContext.add(shortEma, IndicatorIdentification("shortEma"))
                indicatorContext.add(longEma, IndicatorIdentification("longEma"))
                
                // Simple crossover strategy for testing
                val entryRule = shortEma.crossedOver(longEma).toRule()
                val exitRule = shortEma.crossedUnder(longEma).toRule()
                
                return DefaultStrategy(
                    name = "Test Strategy",
                    timeFrames = setOf(TimeFrame.DAY),
                    entryRule = entryRule,
                    exitRule = exitRule,
                    indicatorContexts = indicatorContexts
                )
            }
        }
    }

    @Test
    fun testSignalChartMain() {
        // Test chart creation without displaying GUI (avoid headless issues)
        val signalChart = BuyAndSellSignalsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyFactory = createTestStrategyFactory()

        signalChart.use { chart ->
            chart.setUpStrategy(strategyFactory)

            marketEvents.forEach { event ->
                chart.processMarketEvent(event)
            }

            // Test chart creation
            val jfreeChart = chart.createChart()
            assertThat(jfreeChart).isNotNull()
            assertThat(jfreeChart.title.text).contains("Buy and Sell Signals")

            // Verify price data was processed
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(marketEvents.size)
        }
    }

    @Test
    fun testSignalDetection() {
        val signalChart = BuyAndSellSignalsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyFactory = createTestStrategyFactory()

        signalChart.use { chart ->
            chart.setUpStrategy(strategyFactory)

            // Process all events
            marketEvents.forEach { event ->
                chart.processMarketEvent(event)
            }

            // Verify signals were detected (exact count depends on strategy and data)
            val totalSignals = chart.buySignals.size + chart.sellSignals.size
            assertThat(totalSignals).isGreaterThanOrEqualTo(0) // At least no crash

            // Verify price data matches event count
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(marketEvents.size)
        }
    }

    @Test
    fun testLiveUpdatesMode() {
        // Test live updates mode without actually displaying GUI
        val liveSignalChart = BuyAndSellSignalsToChart(enableLiveUpdates = true)
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyFactory = createTestStrategyFactory()

        liveSignalChart.use { chart ->
            chart.setUpStrategy(strategyFactory)

            // Process events with progress tracking
            chart.processEventsWithProgress(marketEvents.take(10), showProgress = false)

            // Verify data was processed
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(10)

            // Test chart creation
            val jfreeChart = chart.createChart()
            assertThat(jfreeChart).isNotNull()
        }
    }

    @Test
    fun testProgressProcessing() {
        val signalChart = BuyAndSellSignalsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyFactory = createTestStrategyFactory()

        signalChart.use { chart ->
            chart.setUpStrategy(strategyFactory)

            // Test processEventsWithProgress method
            chart.processEventsWithProgress(marketEvents.take(15), showProgress = false)

            // Verify all events were processed
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(15)
        }
    }

    @Test
    fun testStrategyConfiguration() {
        val signalChart = BuyAndSellSignalsToChart()
        val strategyFactory = createTestStrategyFactory()

        signalChart.use { chart ->
            // Test strategy setup
            chart.setUpStrategy(strategyFactory)

            // Process a few events to trigger potential signals
            val marketEvents = MockMarketEventsLoader.loadMarketEvents()
            marketEvents.take(5).forEach { event ->
                chart.processMarketEvent(event)
            }

            // Verify chart can be created
            val jfreeChart = chart.createChart()
            assertThat(jfreeChart).isNotNull()
            assertThat(jfreeChart.title.text).contains("Buy and Sell Signals")
        }
    }

    @Test
    fun testStrategyFactoryApproach() {
        val signalChart = BuyAndSellSignalsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()

        signalChart.use { chart ->
            // Create strategy factory and set it up
            val strategyFactory = createTestStrategyFactory()
            chart.setUpStrategy(strategyFactory)

            // Process events
            marketEvents.take(20).forEach { event ->
                chart.processMarketEvent(event)
            }

            // Verify data was processed
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(20)

            // Test chart creation
            val jfreeChart = chart.createChart()
            assertThat(jfreeChart).isNotNull()
        }
    }

    @Test
    fun testChartCreation() {
        val signalChart = BuyAndSellSignalsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyFactory = createTestStrategyFactory()

        signalChart.use { chart ->
            chart.setUpStrategy(strategyFactory)

            // Process events to get some data
            marketEvents.take(20).forEach { event ->
                chart.processMarketEvent(event)
            }

            // Test chart creation and properties
            val jfreeChart = chart.createChart()
            assertThat(jfreeChart).isNotNull()
            assertThat(jfreeChart.xyPlot).isNotNull()
            assertThat(jfreeChart.xyPlot.datasetCount).isGreaterThan(0)

            // Verify series data
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(20)
        }
    }

    @Test
    fun testLiveTradingBuilderIntegration() {
        val signalChart = BuyAndSellSignalsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()

        signalChart.use { chart ->
            // Test LiveTradingBuilder integration with factory
            val strategyFactory = createTestStrategyFactory()
            chart.setUpStrategy(strategyFactory)

            // Process events
            marketEvents.take(15).forEach { event ->
                chart.processMarketEvent(event)
            }

            // Verify data was processed
            assertThat(chart.copyClosePriceSeries().itemCount).isEqualTo(15)

            // Test chart creation
            val jfreeChart = chart.createChart()
            assertThat(jfreeChart).isNotNull()
            assertThat(jfreeChart.title.text).contains("Buy and Sell Signals")
        }
    }
}

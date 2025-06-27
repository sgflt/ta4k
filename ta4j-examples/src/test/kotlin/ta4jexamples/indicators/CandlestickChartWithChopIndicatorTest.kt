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
package ta4jexamples.indicators

import java.awt.GraphicsEnvironment
import java.util.concurrent.TimeUnit
import org.jfree.chart.plot.CombinedDomainXYPlot
import org.jfree.chart.plot.XYPlot
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class CandlestickChartWithChopIndicatorTest {

    @Test
    @DisplayName("Test main method execution")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun testMainMethod() {
        if (GraphicsEnvironment.isHeadless()) {
            // Skip GUI tests in headless environment
            println("Skipping GUI test in headless environment")
            return
        }
        
        // Test that main method runs without throwing exceptions
        assertDoesNotThrow {
            CandlestickChartWithChopIndicator.main(emptyArray())
        }
    }

    @Test
    @DisplayName("Test candlestick chart with CHOP creation")
    fun testChartCreation() {
        CandlestickChartWithChopIndicator().use { chart ->
            // Process some market events
            val marketEvents = ta4jexamples.loaders.MockMarketEventsLoader.loadMarketEvents(20)
            marketEvents.forEach { marketEvent ->
                chart.processMarketEvent(marketEvent)
            }
            
            // Create the chart
            val jFreeChart = chart.createChart()
            
            // Verify chart properties
            assertNotNull(jFreeChart)
            assertEquals("Bitcoin Price with CHOP Indicator ", jFreeChart.title.text)
            
            // Verify it's a combined plot
            val plot = jFreeChart.plot
            assertNotNull(plot)
            assertTrue(plot is CombinedDomainXYPlot)
            
            val combinedPlot = plot as CombinedDomainXYPlot
            
            // Verify we have 2 subplots (price and indicator)
            assertEquals(2, combinedPlot.subplots.size)
            
            // Verify first subplot is price plot with OHLC data
            val pricePlot = combinedPlot.subplots[0] as XYPlot
            assertNotNull(pricePlot.getDataset(0))
            
            // Verify second subplot is indicator plot
            val indicatorPlot = combinedPlot.subplots[1] as XYPlot
            assertNotNull(indicatorPlot.getDataset(0))
        }
    }

    @Test
    @DisplayName("Test live update mode")
    fun testLiveUpdateMode() {
        CandlestickChartWithChopIndicator(enableLiveUpdates = true, updateDelayMs = 0).use { chart ->
            // Process a few market events
            val marketEvents = ta4jexamples.loaders.MockMarketEventsLoader.loadMarketEvents(5)
            chart.processEventsWithProgress(marketEvents, showProgress = false)
            
            // Create the chart
            val jFreeChart = chart.createChart()
            
            // Verify chart title indicates live mode
            assertTrue(jFreeChart.title.text.contains("(Live)"))
        }
    }

    @Test
    @DisplayName("Test market event processing and CHOP calculation")
    fun testMarketEventProcessingWithChop() {
        CandlestickChartWithChopIndicator().use { chart ->
            // Need at least CHOP_INDICATOR_TIMEFRAME (14) events for CHOP to calculate
            val marketEvents = ta4jexamples.loaders.MockMarketEventsLoader.loadMarketEvents(30)
            
            // Process events
            chart.processEventsWithProgress(marketEvents, showProgress = false)
            
            // Create chart to verify data was processed
            val jFreeChart = chart.createChart()
            val combinedPlot = jFreeChart.plot as CombinedDomainXYPlot
            
            // Verify price plot has data
            val pricePlot = combinedPlot.subplots[0] as XYPlot
            val ohlcDataset = pricePlot.getDataset(0)
            assertNotNull(ohlcDataset)
            assertTrue(ohlcDataset.seriesCount > 0)
            assertTrue(ohlcDataset.getItemCount(0) > 0)
            
            // Verify volume dataset has data
            val volumeDataset = pricePlot.getDataset(1)
            assertNotNull(volumeDataset)
            assertTrue(volumeDataset.seriesCount > 0)
            
            // Verify CHOP indicator plot has data
            val indicatorPlot = combinedPlot.subplots[1] as XYPlot
            val chopDataset = indicatorPlot.getDataset(0)
            assertNotNull(chopDataset)
            assertTrue(chopDataset.seriesCount > 0)
            // CHOP should have data points after the initial period
            assertTrue(chopDataset.getItemCount(0) > 0)
            
            // Note: Annotations are added asynchronously after display,
            // so they won't be present during unit tests
        }
    }

    @Test
    @DisplayName("Test main method with live argument")
    @Timeout(value = 50, unit = TimeUnit.SECONDS)
    fun testMainMethodWithLiveArgument() {
        if (GraphicsEnvironment.isHeadless()) {
            // Skip GUI tests in headless environment
            println("Skipping GUI test in headless environment")
            return
        }
        
        // Test that main method runs with live argument
        assertDoesNotThrow {
            CandlestickChartWithChopIndicator.main(arrayOf("live"))
        }
    }

    @Test
    @DisplayName("Test CHOP indicator threshold annotations")
    fun testChopThresholdAnnotations() {
        CandlestickChartWithChopIndicator().use { chart ->
            // Process enough events for CHOP calculation
            val marketEvents = ta4jexamples.loaders.MockMarketEventsLoader.loadMarketEvents(20)
            chart.processEventsWithProgress(marketEvents, showProgress = false)
            
            // Create the chart
            val jFreeChart = chart.createChart()
            val combinedPlot = jFreeChart.plot as CombinedDomainXYPlot
            val indicatorPlot = combinedPlot.subplots[1] as XYPlot
            
            // Note: Annotations are added asynchronously after display,
            // so we don't verify them in unit tests
            
            // Verify Y-axis range for CHOP indicator
            val yAxis = indicatorPlot.getRangeAxis(0)
            assertEquals(0.0, yAxis.lowerBound, 0.01)
            assertEquals(100.0, yAxis.upperBound, 0.01)
        }
    }
}

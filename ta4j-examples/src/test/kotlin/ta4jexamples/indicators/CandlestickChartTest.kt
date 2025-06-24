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
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class CandlestickChartTest {

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
            CandlestickChart.main(emptyArray())
        }
    }

    @Test
    @DisplayName("Test candlestick chart creation")
    fun testChartCreation() {
        CandlestickChart().use { chart ->
            // Process some market events
            val marketEvents = ta4jexamples.loaders.MockMarketEventsLoader.loadMarketEvents(10)
            marketEvents.forEach { marketEvent ->
                chart.processMarketEvent(marketEvent)
            }
            
            // Create the chart
            val jFreeChart = chart.createChart()
            
            // Verify chart properties
            assertNotNull(jFreeChart)
            assertEquals("Bitcoin Price Chart ", jFreeChart.title.text)
            assertNotNull(jFreeChart.xyPlot)
            
            // Verify plot has correct number of datasets
            val plot = jFreeChart.xyPlot
            assertEquals(2, plot.datasetCount)
            
            // Verify first dataset is OHLC
            assertNotNull(plot.getDataset(0))
            
            // Verify second dataset is time series
            assertNotNull(plot.getDataset(1))
        }
    }

    @Test
    @DisplayName("Test live update mode")
    fun testLiveUpdateMode() {
        CandlestickChart(enableLiveUpdates = true, updateDelayMs = 0).use { chart ->
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
    @DisplayName("Test market event processing")
    fun testMarketEventProcessing() {
        CandlestickChart().use { chart ->
            // Load and process market events
            val marketEvents = ta4jexamples.loaders.MockMarketEventsLoader.loadMarketEvents(20)
            
            // Process events
            chart.processEventsWithProgress(marketEvents, showProgress = false)
            
            // Create chart to verify data was processed
            val jFreeChart = chart.createChart()
            val plot = jFreeChart.xyPlot
            
            // Verify OHLC dataset has data
            val ohlcDataset = plot.getDataset(0)
            assertNotNull(ohlcDataset)
            assertTrue(ohlcDataset.seriesCount > 0)
            assertTrue(ohlcDataset.getItemCount(0) > 0)
            
            // Verify time series dataset has data
            val timeSeriesDataset = plot.getDataset(1)
            assertNotNull(timeSeriesDataset)
            assertTrue(timeSeriesDataset.seriesCount > 0)
        }
    }

    @Test
    @DisplayName("Test main method with live argument")
    fun testMainMethodWithLiveArgument() {
        if (GraphicsEnvironment.isHeadless()) {
            // Skip GUI tests in headless environment
            println("Skipping GUI test in headless environment")
            return
        }
        
        // Test that main method runs with live argument
        assertDoesNotThrow {
            CandlestickChart.main(arrayOf("live"))
        }
    }
}

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

import java.awt.Color
import java.lang.Thread.sleep
import java.time.Instant
import java.util.*
import kotlin.random.Random.Default.nextBoolean
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.ApplicationFrame
import org.jfree.chart.ui.UIUtils
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.trading.preparation.ObservableIndicatorCalculationBuilder
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * This class builds a graphical chart showing values from indicators using modern event-driven architecture.
 * 
 * Migrated from Java to Kotlin with the following improvements:
 * - Uses event-driven architecture with IndicatorContextUpdateListener
 * - Real-time chart updates as bars are processed using ObservableIndicatorCalculationBuilder
 * - JFreeChart visualization with Bollinger Bands and technical indicators
 * - Type-safe indicator identification
 * - Modern Kotlin implementation with proper resource management
 */
class IndicatorsToChart(
    private val timeFrame: TimeFrame = TimeFrame.DAY,
    private val enableLiveUpdates: Boolean = false,
    private val updateDelayMs: Long = 100
) : IndicatorContextUpdateListener, AutoCloseable {

    private var lastUpdateTime: Instant = Instant.MIN
    private val indicatorContexts: IndicatorContexts
    private val marketEventHandler: MarketEventHandler
    
    // JFreeChart time series for real-time updates
    private val closePriceSeries = TimeSeries("Close Price")
    private val ema14Series = TimeSeries("EMA (14)")
    private val bollingerUpperSeries = TimeSeries("Bollinger Upper")
    private val bollingerLowerSeries = TimeSeries("Bollinger Lower")
    
    // Chart components for live updates
    private var chartPanel: ChartPanel? = null
    private var applicationFrame: ApplicationFrame? = null
    private var chart: JFreeChart? = null

    // Indicator identifications for type-safe access
    private val indicators = mapOf(
        "closePrice" to CLOSE_PRICE,
        "ema14" to EMA_14,
        "standardDev14" to STANDARD_DEVIATION_14,
        "bollingerUpper" to BOLLINGER_UPPER,
        "bollingerMiddle" to BOLLINGER_MIDDLE,
        "bollingerLower" to BOLLINGER_LOWER
    )

    init {
        // Setup indicator contexts first
        indicatorContexts = setupIndicatorContexts()
        
        // Setup market event handler with this class as listener
        marketEventHandler = ObservableIndicatorCalculationBuilder()
            .withIndicatorContexts(indicatorContexts)
            .withIndicatorContextUpdateListener(this)
            .build()
    }

    private fun setupIndicatorContexts(): IndicatorContexts {
        val contexts = IndicatorContexts.empty().apply {
            add(IndicatorContext.empty(timeFrame))
        }
        
        val indicatorContext = contexts[timeFrame]
        
        // Core price indicators
        val closePrice = Indicators.closePrice()
        
        // Technical indicators
        val ema14 = closePrice.ema(14)
        val standardDev14 = closePrice.stddev(14)
        
        // Bollinger Bands (using EMA as middle band and standard deviation)
        val bollingerMiddle = ema14  // EMA serves as middle band
        val bollingerUpper = ema14.plus(standardDev14.multipliedBy(2.0))  // Upper: EMA + 2*StdDev
        val bollingerLower = ema14.minus(standardDev14.multipliedBy(2.0))  // Lower: EMA - 2*StdDev

        // Register all indicators with the context
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(ema14, EMA_14)
        indicatorContext.add(standardDev14, STANDARD_DEVIATION_14)
        indicatorContext.add(bollingerUpper, BOLLINGER_UPPER)
        indicatorContext.add(bollingerMiddle, BOLLINGER_MIDDLE)
        indicatorContext.add(bollingerLower, BOLLINGER_LOWER)
        
        return contexts
    }

    override fun onContextUpdate(time: Instant) {
        // Ensure idempotency - only process each time once
        if (time.isAfter(lastUpdateTime)) {
            lastUpdateTime = time
            
            try {

                // If live updates are enabled and chart is displayed, trigger repaint
                if (nextBoolean() && enableLiveUpdates && chartPanel != null) {
                    updateChartSeries(time)
                    sleep(updateDelayMs)
                    
                    // Use SwingUtilities to ensure UI updates happen on EDT
                    javax.swing.SwingUtilities.invokeLater {
                        chartPanel?.repaint()
                    }
                }
            } catch (e: Exception) {
                println("Error updating chart at $time: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateChartSeries(time: Instant) {
        val context = indicatorContexts[timeFrame]
        val day = Day(Date.from(time))
        
        // Update close price series
        context.getNumericIndicator(CLOSE_PRICE)?.value?.doubleValue()?.let { closePrice ->
            closePriceSeries.addOrUpdate(day, closePrice)
        }
        
        // Update EMA series
        context.getNumericIndicator(EMA_14)?.value?.doubleValue()?.let { ema14 ->
            ema14Series.addOrUpdate(day, ema14)
        }
        
        // Update Bollinger Bands
        context.getNumericIndicator(BOLLINGER_UPPER)?.value?.doubleValue()?.let { upper ->
            bollingerUpperSeries.addOrUpdate(day, upper)
        }
        
        context.getNumericIndicator(BOLLINGER_LOWER)?.value?.doubleValue()?.let { lower ->
            bollingerLowerSeries.addOrUpdate(day, lower)
        }
    }

    fun processMarketEvent(marketEvent: CandleReceived) {
        marketEventHandler.onCandle(marketEvent)
    }

    fun createChart(): JFreeChart {
        // Create dataset
        val dataset = TimeSeriesCollection().apply {
            addSeries(closePriceSeries)
            addSeries(ema14Series)
            addSeries(bollingerUpperSeries)
            addSeries(bollingerLowerSeries)
        }

        // Create chart
        val chart = ChartFactory.createTimeSeriesChart(
            "Technical Analysis - Close Price with Bollinger Bands", // title
            "Date", // x-axis label
            "Price", // y-axis label
            dataset, // data
            true, // create legend?
            true, // generate tooltips?
            false // generate URLs?
        )

        // Customize chart appearance
        val plot = chart.xyPlot as XYPlot
        val renderer = XYLineAndShapeRenderer(true, false)
        
        // Set colors for different series
        renderer.setSeriesPaint(0, Color.BLUE)    // Close price - blue
        renderer.setSeriesPaint(1, Color.RED)     // EMA - red
        renderer.setSeriesPaint(2, Color.GREEN)   // Upper Bollinger - green
        renderer.setSeriesPaint(3, Color.GREEN)   // Lower Bollinger - green
        
        plot.renderer = renderer
        plot.backgroundPaint = Color.WHITE
        plot.rangeGridlinePaint = Color.LIGHT_GRAY

        return chart
    }

    fun displayChart() {
        chart = createChart()
        
        // Chart panel
        chartPanel = ChartPanel(chart).apply {
            fillZoomRectangle = true
            isMouseWheelEnabled = true
            preferredSize = java.awt.Dimension(800, 600)
        }
        
        // Application frame
        applicationFrame = ApplicationFrame("Ta4j Example - Indicators to Chart ${if (enableLiveUpdates) "(Live Updates)" else ""}").apply {
            contentPane = chartPanel
            pack()
            UIUtils.centerFrameOnScreen(this)
            isVisible = true
        }
        
        if (enableLiveUpdates) {
            println("Live chart updates enabled. Chart will update in real-time as data is processed.")
        }
    }
    
    fun processEventsWithProgress(marketEvents: List<CandleReceived>, showProgress: Boolean = true) {
        if (showProgress) {
            println("Processing ${marketEvents.size} market events with live chart updates...")
        }
        
        marketEvents.forEachIndexed { index, marketEvent ->
            processMarketEvent(marketEvent)
            
            if (showProgress && (index + 1) % 10 == 0) {
                println("Processed ${index + 1}/${marketEvents.size} events...")
            }
        }
        
        if (showProgress) {
            println("Completed processing all ${marketEvents.size} market events.")
        }
    }

    override fun close() {
        // Close application frame if it exists
        applicationFrame?.dispose()
        
        println("Chart data processing completed.")
        println("Close price data points: ${closePriceSeries.itemCount}")
        println("EMA data points: ${ema14Series.itemCount}")
        println("Bollinger Bands data points: ${bollingerUpperSeries.itemCount}")
    }

    companion object {
        // Indicator identifications for type-safe access
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")
        private val EMA_14 = IndicatorIdentification("ema14")
        private val STANDARD_DEVIATION_14 = IndicatorIdentification("standardDev14")
        private val BOLLINGER_UPPER = IndicatorIdentification("bollingerUpper")
        private val BOLLINGER_MIDDLE = IndicatorIdentification("bollingerMiddle")
        private val BOLLINGER_LOWER = IndicatorIdentification("bollingerLower")

        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting Indicators to Chart visualization using IndicatorContext listeners...")
            
            // Get market data
            val marketEvents = MockMarketEventsLoader.loadMarketEvents(1000)
            
            // Check if user wants live updates (can be passed as argument)
            val enableLive = args.isNotEmpty() && args[0].lowercase() == "live"
            
            if (enableLive) {
                println("=== LIVE UPDATE MODE ===")
                // Create chart generator with live updates enabled
                IndicatorsToChart(enableLiveUpdates = true, updateDelayMs = 200).use { liveGenerator ->
                    // Display chart first (empty)
                    liveGenerator.displayChart()
                    
                    // Process events with progress - chart will update in real-time
                    liveGenerator.processEventsWithProgress(marketEvents)
                    
                    println("Live chart updates completed! Chart shows ${liveGenerator.closePriceSeries.itemCount} data points.")
                    println("Press Enter to continue...")
                    readLine() // Keep chart open for viewing
                }
            } else {
                println("=== STATIC MODE ===")
                // Create the chart generator with standard mode
                IndicatorsToChart().use { chartGenerator ->
                    // Process each market event - this will trigger the listener and update chart data
                    println("Processing ${marketEvents.size} market events...")
                    marketEvents.forEach { marketEvent ->
                        chartGenerator.processMarketEvent(marketEvent)
                    }
                    
                    // Display the chart
                    println("Displaying chart with ${chartGenerator.closePriceSeries.itemCount} data points...")
                    chartGenerator.displayChart()
                }
            }
            
            println("Chart visualization completed!")
            println("Tip: Run with 'live' argument to see real-time chart updates!")
        }
    }
}

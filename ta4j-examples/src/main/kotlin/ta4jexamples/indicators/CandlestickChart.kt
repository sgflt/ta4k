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
import java.time.Instant
import java.util.*
import javax.swing.SwingUtilities
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.CandlestickRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.ApplicationFrame
import org.jfree.chart.ui.UIUtils
import org.jfree.data.time.Second
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.DefaultHighLowDataset
import org.jfree.data.xy.OHLCDataset
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.events.TickReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.trading.preparation.ObservableIndicatorCalculationBuilder
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * This class builds a traditional candlestick chart.
 * 
 * Migrated from Java to Kotlin with the following improvements:
 * - Uses event-driven architecture with real-time updates
 * - Replaced CsvTradesLoader with MockMarketEventsLoader
 * - Added support for live chart updates
 * - Improved resource management with AutoCloseable
 * - Kotlin idioms and null safety
 */
class CandlestickChart(
    private val timeFrame: TimeFrame = TimeFrame.DAY,
    private val enableLiveUpdates: Boolean = false,
    private val updateDelayMs: Long = 100
) : IndicatorContextUpdateListener, AutoCloseable {

    private var lastUpdateTime: Instant = Instant.MIN
    private val indicatorContexts: IndicatorContexts
    private val marketEventHandler: MarketEventHandler
    
    // Chart components
    private var chartPanel: ChartPanel? = null
    private var applicationFrame: ApplicationFrame? = null
    private var chart: JFreeChart? = null
    
    // Data storage for OHLC
    private data class OHLCData(
        val time: Instant,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Double
    )
    
    private val ohlcDataList = mutableListOf<OHLCData>()
    private val closePriceTimeSeries = TimeSeries("BTC Price")
    
    // Indicator identification
    private val CLOSE_PRICE = IndicatorIdentification("closePrice")

    init {
        indicatorContexts = setupIndicatorContexts()
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
        val closePrice = Indicators.closePrice()
        indicatorContext.add(closePrice, CLOSE_PRICE)
        
        return contexts
    }

    override fun onContextUpdate(time: Instant) {
        if (time.isAfter(lastUpdateTime)) {
            lastUpdateTime = time
            
            // Update chart if live updates are enabled
            if (enableLiveUpdates && chartPanel != null && ohlcDataList.isNotEmpty()) {
                SwingUtilities.invokeLater {
                    updateChart()
                    chartPanel?.repaint()
                }
            }
        }
    }

    private fun createOHLCDataset(): OHLCDataset {
        val nbBars = ohlcDataList.size
        
        val dates = Array(nbBars) { i ->
            Date(ohlcDataList[i].time.toEpochMilli())
        }
        
        val highs = DoubleArray(nbBars) { i -> ohlcDataList[i].high }
        val lows = DoubleArray(nbBars) { i -> ohlcDataList[i].low }
        val opens = DoubleArray(nbBars) { i -> ohlcDataList[i].open }
        val closes = DoubleArray(nbBars) { i -> ohlcDataList[i].close }
        val volumes = DoubleArray(nbBars) { i -> ohlcDataList[i].volume }
        
        return DefaultHighLowDataset("BTC", dates, highs, lows, opens, closes, volumes)
    }

    private fun createAdditionalDataset(): TimeSeriesCollection {
        return TimeSeriesCollection().apply {
            addSeries(closePriceTimeSeries)
        }
    }

    private fun updateChart() {
        chart?.let { existingChart ->
            val plot = existingChart.xyPlot
            
            // Update OHLC dataset
            plot.setDataset(0, createOHLCDataset())
            
            // Update additional dataset
            plot.setDataset(1, createAdditionalDataset())
        }
    }

    fun createChart(): JFreeChart {
        // Create OHLC dataset
        val ohlcDataset = createOHLCDataset()
        
        // Create additional dataset
        val xyDataset = createAdditionalDataset()
        
        // Create the chart
        val chart = ChartFactory.createCandlestickChart(
            "Bitcoin Price Chart ${if (enableLiveUpdates) "(Live)" else ""}",
            "Time",
            "USD",
            ohlcDataset,
            true
        )
        
        // Candlestick rendering
        val renderer = CandlestickRenderer().apply {
            autoWidthMethod = CandlestickRenderer.WIDTHMETHOD_SMALLEST
        }
        
        val plot = chart.xyPlot.apply {
            setRenderer(renderer)
            
            // Additional dataset
            setDataset(1, xyDataset)
            mapDatasetToRangeAxis(1, 0)
            
            val renderer2 = XYLineAndShapeRenderer(true, false).apply {
                setSeriesPaint(0, Color.BLUE)
            }
            setRenderer(1, renderer2)
            
            // Styling
            rangeGridlinePaint = Color.LIGHT_GRAY
            backgroundPaint = Color.WHITE
            datasetRenderingOrder = DatasetRenderingOrder.FORWARD
        }
        
        // Configure number axis
        (plot.rangeAxis as NumberAxis).autoRangeIncludesZero = false
        
        return chart
    }

    fun displayChart() {
        chart = createChart()
        
        // Chart panel
        chartPanel = ChartPanel(chart).apply {
            fillZoomRectangle = true
            isMouseWheelEnabled = true
            preferredSize = java.awt.Dimension(740, 300)
        }
        
        // Application frame
        applicationFrame = ApplicationFrame("Ta4j Example - Candlestick Chart").apply {
            contentPane = chartPanel
            pack()
            UIUtils.centerFrameOnScreen(this)
            isVisible = true
        }
        
        if (enableLiveUpdates) {
            println("Live candlestick chart updates enabled.")
        }
    }

    fun processMarketEvent(marketEvent: CandleReceived) {
        // Store OHLC data from the event
        val ohlcData = OHLCData(
            time = marketEvent.endTime,
            open = marketEvent.openPrice,
            high = marketEvent.highPrice,
            low = marketEvent.lowPrice,
            close = marketEvent.closePrice,
            volume = marketEvent.volume
        )
        ohlcDataList.add(ohlcData)
        
        // Update time series for additional dataset
        val second = Second(Date(marketEvent.endTime.toEpochMilli()))
        closePriceTimeSeries.addOrUpdate(second, marketEvent.closePrice)
        
        // Process through handler for indicator updates
        marketEventHandler.onCandle(marketEvent)
    }

    fun processEventsWithProgress(marketEvents: List<CandleReceived>, showProgress: Boolean = true) {
        if (showProgress) {
            println("Processing ${marketEvents.size} market events...")
        }
        
        marketEvents.forEachIndexed { index, marketEvent ->
            processMarketEvent(marketEvent)
            
            if (showProgress && (index + 1) % 100 == 0) {
                println("Processed ${index + 1}/${marketEvents.size} events...")
            }
            
            if (enableLiveUpdates && updateDelayMs > 0) {
                Thread.sleep(updateDelayMs)
            }
        }
        
        if (showProgress) {
            println("Completed processing ${marketEvents.size} market events.")
        }
    }

    override fun close() {
        applicationFrame?.dispose()
        println("Candlestick chart closed. Processed ${ohlcDataList.size} bars.")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting Candlestick Chart example...")
            
            // Load market data
            val marketEvents = MockMarketEventsLoader.loadMarketEvents(500)
            
            // Check if user wants live updates
            val enableLive = args.isNotEmpty() && args[0].lowercase() == "live"
            
            if (enableLive) {
                println("=== LIVE UPDATE MODE ===")
                CandlestickChart(enableLiveUpdates = true, updateDelayMs = 50).use { chartGenerator ->
                    // Display empty chart first
                    chartGenerator.displayChart()
                    
                    // Process events with live updates
                    chartGenerator.processEventsWithProgress(marketEvents)
                    
                    println("Live candlestick chart completed with ${chartGenerator.ohlcDataList.size} bars.")
                    println("Press Enter to close...")
                    readLine()
                }
            } else {
                println("=== STATIC MODE ===")
                CandlestickChart().use { chartGenerator ->
                    // Process all events first
                    chartGenerator.processEventsWithProgress(marketEvents, showProgress = false)
                    
                    // Then display the complete chart
                    println("Displaying candlestick chart with ${chartGenerator.ohlcDataList.size} bars...")
                    chartGenerator.displayChart()
                }
            }
            
            println("Candlestick chart example completed!")
            println("Tip: Run with 'live' argument to see real-time updates!")
        }
    }
}
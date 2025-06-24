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

import java.awt.BasicStroke
import java.awt.Color
import java.awt.GridLayout
import java.time.Instant
import java.util.*
import javax.swing.SwingUtilities
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.annotations.XYLineAnnotation
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.CombinedDomainXYPlot
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.PlotOrientation
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
import org.jfree.data.xy.XYDataset
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
 * This class builds a traditional candlestick chart with CHOP indicator.
 * 
 * Migrated from Java to Kotlin with the following improvements:
 * - Event-driven architecture with real-time updates
 * - Replaced CsvTradesLoader with MockMarketEventsLoader
 * - Modern Indicators API usage
 * - Support for live chart updates
 * - Improved resource management with AutoCloseable
 * 
 * The CHOP (Choppiness Index) indicator measures market choppiness:
 * - Values above 61.8 indicate choppy/sideways market (avoid trading)
 * - Values below 38.2 indicate trending market (good for trading)
 */
class CandlestickChartWithChopIndicator(
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
    private var combinedChart: JFreeChart? = null
    private var combinedPlot: CombinedDomainXYPlot? = null
    private var indicatorXYPlot: XYPlot? = null
    
    // Data storage
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
    private val chopTimeSeries = TimeSeries("CHOP_$CHOP_INDICATOR_TIMEFRAME")
    
    // Indicator identifications
    private val CLOSE_PRICE = IndicatorIdentification("closePrice")
    private val CHOP = IndicatorIdentification("chop")

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
        
        // Create indicators
        val closePrice = Indicators.closePrice()
        val chopIndicator = Indicators.chop(CHOP_INDICATOR_TIMEFRAME, CHOP_SCALE_VALUE)
        
        // Register indicators
        indicatorContext.add(closePrice, CLOSE_PRICE)
        indicatorContext.add(chopIndicator, CHOP)
        
        return contexts
    }

    override fun onContextUpdate(time: Instant) {
        if (time.isAfter(lastUpdateTime)) {
            lastUpdateTime = time
            
            // Update CHOP indicator series if we have enough data
            if (ohlcDataList.size >= CHOP_INDICATOR_TIMEFRAME) {
                val context = indicatorContexts[timeFrame]
                context.getNumericIndicator(CHOP)?.value?.doubleValue()?.let { chopValue ->
                    val second = Second(Date(time.toEpochMilli()))
                    chopTimeSeries.addOrUpdate(second, chopValue)
                }
            }
            
            // Update chart if live updates are enabled
            if (enableLiveUpdates && chartPanel != null && ohlcDataList.isNotEmpty()) {
                SwingUtilities.invokeLater {
                    updateChart()
                    chartPanel?.repaint()
                }
            }
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
        
        // Update close price time series
        val second = Second(Date(marketEvent.endTime.toEpochMilli()))
        closePriceTimeSeries.addOrUpdate(second, marketEvent.closePrice)
        
        // Process through handler for indicator updates
        marketEventHandler.onCandle(marketEvent)
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

    private fun createChopDataset(): TimeSeriesCollection {
        return TimeSeriesCollection().apply {
            addSeries(chopTimeSeries)
        }
    }

    private fun updateChart() {
        combinedPlot?.let { plot ->
            // Update price plot datasets
            val pricePlot = plot.subplots.firstOrNull() as? XYPlot
            pricePlot?.let {
                it.setDataset(0, createOHLCDataset())
                it.setDataset(VOLUME_DATASET_INDEX, createAdditionalDataset())
            }
            
            // Update indicator plot dataset
            indicatorXYPlot?.setDataset(0, createChopDataset())
        }
    }

    fun createChart(): JFreeChart {
        val xAxis = DateAxis("Time")
        
        // Create OHLC dataset
        val ohlcDataset = createOHLCDataset()
        val xyDataset = createAdditionalDataset()
        val chopDataset = createChopDataset()
        
        // Create price plot with candlestick renderer
        val renderer = CandlestickRenderer().apply {
            autoWidthMethod = CandlestickRenderer.WIDTHMETHOD_SMALLEST
        }
        
        val pricePlot = XYPlot(ohlcDataset, xAxis, NumberAxis("Price"), renderer).apply {
            // Add volume dataset
            setDataset(VOLUME_DATASET_INDEX, xyDataset)
            mapDatasetToRangeAxis(VOLUME_DATASET_INDEX, 0)
            
            val renderer2 = XYLineAndShapeRenderer(true, false).apply {
                setSeriesPaint(0, Color.BLUE)
            }
            setRenderer(VOLUME_DATASET_INDEX, renderer2)
            
            // Styling
            rangeGridlinePaint = Color.LIGHT_GRAY
            backgroundPaint = Color.WHITE
            datasetRenderingOrder = DatasetRenderingOrder.FORWARD
            
            (rangeAxis as NumberAxis).autoRangeIncludesZero = false
        }
        
        // Create CHOP indicator plot
        indicatorXYPlot = XYPlot().apply {
            setDataset(0, chopDataset)
            
            val yAxis = NumberAxis("CHOP").apply {
                setRange(0.0, CHOP_SCALE_VALUE.toDouble())
            }
            setRangeAxis(0, yAxis)
            setRenderer(0, XYLineAndShapeRenderer(true, false))
        }
        
        // Create combined plot
        combinedPlot = CombinedDomainXYPlot(xAxis).apply {
            gap = 10.0
            backgroundPaint = Color.LIGHT_GRAY
            domainGridlinePaint = Color.GRAY
            rangeGridlinePaint = Color.GRAY
            orientation = PlotOrientation.VERTICAL
            
            // Add sub-plots with weights
            add(pricePlot, 70)
            add(indicatorXYPlot, 30)
        }
        
        // Create the chart
        combinedChart = JFreeChart(
            "Bitcoin Price with CHOP Indicator ${if (enableLiveUpdates) "(Live)" else ""}",
            null,
            combinedPlot,
            true
        ).apply {
            backgroundPaint = Color.LIGHT_GRAY
        }
        
        return combinedChart!!
    }

    fun displayChart() {
        val chart = createChart()
        
        // Chart panel
        chartPanel = ChartPanel(chart).apply {
            layout = GridLayout(0, 1)
            background = Color.LIGHT_GRAY
            preferredSize = java.awt.Dimension(740, 300)
        }
        
        // Application frame
        applicationFrame = ApplicationFrame("Ta4j Example - Candlestick Chart with CHOP").apply {
            contentPane = chartPanel
            pack()
            UIUtils.centerFrameOnScreen(this)
            isVisible = true
        }
        
        // Add threshold annotations after the chart is displayed
        SwingUtilities.invokeLater {
            addThresholdAnnotations()
        }
        
        if (enableLiveUpdates) {
            println("Live chart updates enabled for candlestick with CHOP indicator.")
        }
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

    private fun addThresholdAnnotations() {
        indicatorXYPlot?.let { plot ->
            // Get the actual domain range from the plot
            val domainAxis = combinedPlot?.domainAxis
            domainAxis?.let { axis ->
                val range = axis.range
                if (range != null) {
                    val lowerBound = range.lowerBound
                    val upperBound = range.upperBound
                    
                    // Lower threshold line (38.2 - trending market)
                    val lowerLine = XYLineAnnotation(
                        lowerBound, CHOP_LOWER_THRESHOLD,
                        upperBound, CHOP_LOWER_THRESHOLD,
                        DASHED_THIN_LINE_STYLE, Color.GREEN
                    ).apply {
                        toolTipText = "Tradable below this (trending market)"
                    }
                    plot.addAnnotation(lowerLine)
                    
                    // Upper threshold line (61.8 - choppy market)
                    val upperLine = XYLineAnnotation(
                        lowerBound, CHOP_UPPER_THRESHOLD,
                        upperBound, CHOP_UPPER_THRESHOLD,
                        DASHED_THIN_LINE_STYLE, Color.RED
                    ).apply {
                        toolTipText = "Too choppy above this (sideways market)"
                    }
                    plot.addAnnotation(upperLine)
                }
            }
        }
    }
    
    override fun close() {
        applicationFrame?.dispose()
        println("Candlestick chart with CHOP indicator closed. Processed ${ohlcDataList.size} bars.")
    }

    companion object {
        private const val CHOP_INDICATOR_TIMEFRAME = 14
        private const val CHOP_UPPER_THRESHOLD = 61.8
        private const val CHOP_LOWER_THRESHOLD = 38.2
        private const val VOLUME_DATASET_INDEX = 1
        private const val CHOP_SCALE_VALUE = 100
        
        private val DASHED_THIN_LINE_STYLE = BasicStroke(
            0.4f, 
            BasicStroke.CAP_ROUND, 
            BasicStroke.JOIN_ROUND, 
            1.0f,
            floatArrayOf(8.0f, 4.0f), 
            0.0f
        )
        
        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting Candlestick Chart with CHOP Indicator example...")
            
            // Load market data
            val marketEvents = MockMarketEventsLoader.loadMarketEvents(500)
            
            // Check if user wants live updates
            val enableLive = args.isNotEmpty() && args[0].lowercase() == "live"
            
            if (enableLive) {
                println("=== LIVE UPDATE MODE ===")
                CandlestickChartWithChopIndicator(enableLiveUpdates = true, updateDelayMs = 50).use { chartGenerator ->
                    // Display empty chart first
                    chartGenerator.displayChart()
                    
                    // Process events with live updates
                    chartGenerator.processEventsWithProgress(marketEvents)
                    
                    println("Live chart completed with ${chartGenerator.ohlcDataList.size} bars.")
                    println("CHOP indicator values: ${chartGenerator.chopTimeSeries.itemCount}")
                    println("Press Enter to close...")
                    readLine()
                }
            } else {
                println("=== STATIC MODE ===")
                CandlestickChartWithChopIndicator().use { chartGenerator ->
                    // Process all events first
                    chartGenerator.processEventsWithProgress(marketEvents, showProgress = false)
                    
                    // Then display the complete chart
                    println("Displaying candlestick chart with CHOP indicator...")
                    println("CHOP values available: ${chartGenerator.chopTimeSeries.itemCount}")
                    chartGenerator.displayChart()
                }
            }
            
            println("Candlestick chart with CHOP indicator example completed!")
            println("Tip: Run with 'live' argument to see real-time updates!")
        }
    }
}
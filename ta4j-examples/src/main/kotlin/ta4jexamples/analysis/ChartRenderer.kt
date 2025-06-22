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
package ta4jexamples.analysis

import java.awt.Color
import java.awt.Font
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.SwingUtilities
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.annotations.XYPointerAnnotation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.ApplicationFrame
import org.jfree.chart.ui.UIUtils
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection

/**
 * Thread-safe chart renderer for buy/sell signals with real-time updates.
 *
 * This class handles all JFreeChart operations safely by:
 * - Using a single TimeSeries with proper synchronization
 * - Queuing updates and processing them on the AWT thread
 * - Maintaining separate collections for buy/sell signals
 * - Providing simple API for price and signal updates
 */
class ChartRenderer(
    private val enableLiveUpdates: Boolean = false,
) : AutoCloseable {

    // Chart components
    private var chart: JFreeChart? = null
    private var chartPanel: ChartPanel? = null
    private var applicationFrame: ApplicationFrame? = null
    private var plot: XYPlot? = null

    // Thread-safe price data
    private val priceTimeSeries = TimeSeries("Close Price")

    // Signal collections - thread-safe
    private val buySignals = ConcurrentLinkedQueue<SignalData>()
    private val sellSignals = ConcurrentLinkedQueue<SignalData>()

    // Update queues for thread-safe operations
    private val priceUpdates = ConcurrentLinkedQueue<PriceUpdate>()
    private val signalUpdates = ConcurrentLinkedQueue<SignalUpdate>()

    @Volatile
    private var lastUpdateTime = 0L
    private val updateIntervalMs = 25L // Update every 25ms max for smoother live updates

    data class SignalData(val time: Instant, val price: Double)
    data class PriceUpdate(val time: Instant, val price: Double)
    data class SignalUpdate(val signal: SignalData, val isBuy: Boolean)

    /**
     * Add a price data point
     */
    fun addPriceData(time: Instant, price: Double) {
        if (enableLiveUpdates) {
            priceUpdates.offer(PriceUpdate(time, price))
            scheduleUpdate()
        } else {
            // In static mode, add directly to time series
            val day = Day(Date.from(time))
            priceTimeSeries.addOrUpdate(day, price)
        }
    }

    /**
     * Add a buy signal
     */
    fun addBuySignal(time: Instant, price: Double) {
        val signalData = SignalData(time, price)
        buySignals.offer(signalData)

        if (enableLiveUpdates) {
            signalUpdates.offer(SignalUpdate(signalData, true))
            scheduleUpdate()
        }
    }

    /**
     * Add a sell signal
     */
    fun addSellSignal(time: Instant, price: Double) {
        val signalData = SignalData(time, price)
        sellSignals.offer(signalData)

        if (enableLiveUpdates) {
            signalUpdates.offer(SignalUpdate(signalData, false))
            scheduleUpdate()
        }
    }

    /**
     * Schedule chart update on AWT thread if needed
     */
    private fun scheduleUpdate() {
        if (!enableLiveUpdates) return

        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < updateIntervalMs) return

        lastUpdateTime = now
        SwingUtilities.invokeLater { processUpdates() }
    }

    /**
     * Process all pending updates on AWT thread
     */
    private fun processUpdates() {
        var priceUpdated = false
        
        // Process one price update per call to limit queue processing
        priceUpdates.poll()?.run {
            val day = Day(Date.from(time))
            priceTimeSeries.addOrUpdate(day, price)
            priceUpdated = true
        }

        // Process one signal update per call
        signalUpdates.poll()?.run {
            if (isBuy) {
                addBuyMarkerToChart(signal.time, signal.price)
            } else {
                addSellMarkerToChart(signal.time, signal.price)
            }
        }

        // Notify dataset of changes if price was updated
        if (priceUpdated) {
            priceTimeSeries.fireSeriesChanged()
        }
        
        chartPanel?.repaint()
    }

    /**
     * Create the JFreeChart
     */
    fun createChart(): JFreeChart {
        val dataset = TimeSeriesCollection().apply {
            addSeries(priceTimeSeries)
        }

        chart = ChartFactory.createTimeSeriesChart(
            "Buy and Sell Signals",
            "Date",
            "Price",
            dataset,
            true,
            true,
            false
        )

        plot = chart!!.xyPlot as XYPlot
        val renderer = XYLineAndShapeRenderer(true, false)
        renderer.setSeriesPaint(0, Color.BLUE)

        plot!!.renderer = renderer
        plot!!.backgroundPaint = Color.WHITE
        plot!!.rangeGridlinePaint = Color.LIGHT_GRAY

        // Enable auto-scaling for live updates
        if (enableLiveUpdates) {
            plot!!.domainAxis.setAutoRange(true)
            plot!!.rangeAxis.setAutoRange(true)
        }

        // Add existing signals if not in live mode
        if (!enableLiveUpdates) {
            addAllExistingSignals()
        }

        return chart!!
    }

    /**
     * Display the chart in a window
     */
    fun displayChart() {
        val createdChart = createChart()

        chartPanel = ChartPanel(createdChart).apply {
            fillZoomRectangle = true
            isMouseWheelEnabled = true
            preferredSize = java.awt.Dimension(1024, 600)
        }

        applicationFrame = ApplicationFrame(
            "Ta4j Example - Buy and Sell Signals ${if (enableLiveUpdates) "(Live)" else ""}"
        ).apply {
            contentPane = chartPanel
            pack()
            UIUtils.centerFrameOnScreen(this)
            isVisible = true
        }
    }

    /**
     * Add buy marker to chart (must be called on AWT thread)
     */
    private fun addBuyMarkerToChart(time: Instant, price: Double) {
        plot?.let { xyPlot ->
            // Green upward arrow at exact price level
            val timeValue = Day(Date.from(time)).firstMillisecond.toDouble()
            val buyArrow = XYPointerAnnotation(
                "BUY \$${String.format("%.2f", price)}",
                timeValue,
                price,
                Math.PI / 2 // Point upward (90 degrees)
            ).apply {
                setArrowPaint(Color.GREEN)
                setArrowStroke(java.awt.BasicStroke(2.0f))
                setArrowLength(10.0)
                setBaseRadius(5.0)
                setFont(Font("SansSerif", Font.BOLD, 9))
                setPaint(Color.GREEN)
                setTextAnchor(org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER)
            }
            xyPlot.addAnnotation(buyArrow)
        }
    }

    /**
     * Add sell marker to chart (must be called on AWT thread)
     */
    private fun addSellMarkerToChart(time: Instant, price: Double) {
        plot?.let { xyPlot ->
            // Red downward arrow at exact price level
            val timeValue = Day(Date.from(time)).firstMillisecond.toDouble()
            val sellArrow = XYPointerAnnotation(
                "SELL \$${String.format("%.2f", price)}",
                timeValue,
                price,
                -Math.PI / 2 // Point downward (-90 degrees)
            ).apply {
                setArrowPaint(Color.RED)
                setArrowStroke(java.awt.BasicStroke(2.0f))
                setArrowLength(10.0)
                setBaseRadius(5.0)
                setFont(Font("SansSerif", Font.BOLD, 9))
                setPaint(Color.RED)
                setTextAnchor(org.jfree.chart.ui.TextAnchor.TOP_CENTER)
            }
            xyPlot.addAnnotation(sellArrow)
        }
    }

    /**
     * Add all existing signals to chart
     */
    private fun addAllExistingSignals() {
        buySignals.forEach { signal ->
            addBuyMarkerToChart(signal.time, signal.price)
        }
        sellSignals.forEach { signal ->
            addSellMarkerToChart(signal.time, signal.price)
        }
    }

    /**
     * Get copy of price series for testing
     */
    fun getPriceSeriesCopy(): TimeSeries {
        // In live mode, process ALL pending updates first for accurate testing
        if (enableLiveUpdates) {
            javax.swing.SwingUtilities.invokeAndWait { 
                // Process all remaining price updates for testing
                while (true) {
                    val update = priceUpdates.poll() ?: break
                    val day = Day(Date.from(update.time))
                    priceTimeSeries.addOrUpdate(day, update.price)
                }
                priceTimeSeries.fireSeriesChanged()
            }
        }

        return if (priceTimeSeries.itemCount == 0) {
            TimeSeries("Empty")
        } else {
            priceTimeSeries.createCopy(0, priceTimeSeries.itemCount - 1)
        }
    }

    /**
     * Get buy signals count
     */
    fun getBuySignalsCount(): Int = buySignals.size

    /**
     * Get sell signals count
     */
    fun getSellSignalsCount(): Int = sellSignals.size

    /**
     * Get all buy signals
     */
    fun getBuySignals(): List<SignalData> = buySignals.toList()

    /**
     * Get all sell signals
     */
    fun getSellSignals(): List<SignalData> = sellSignals.toList()

    /**
     * Force chart update - useful at end of processing
     */
    fun forceUpdate() {
        if (enableLiveUpdates) {
            SwingUtilities.invokeLater {
                // Process ALL remaining updates for final display
                var priceUpdated = false
                
                // Process all remaining price updates
                while (true) {
                    val update = priceUpdates.poll() ?: break
                    val day = Day(Date.from(update.time))
                    priceTimeSeries.addOrUpdate(day, update.price)
                    priceUpdated = true
                }
                
                // Process all remaining signal updates
                while (true) {
                    val update = signalUpdates.poll() ?: break
                    if (update.isBuy) {
                        addBuyMarkerToChart(update.signal.time, update.signal.price)
                    } else {
                        addSellMarkerToChart(update.signal.time, update.signal.price)
                    }
                }
                
                // Notify of changes
                if (priceUpdated) {
                    priceTimeSeries.fireSeriesChanged()
                }
                
                // Force axis auto-range to show all data
                plot?.let {
                    it.domainAxis.setAutoRange(true)
                    it.rangeAxis.setAutoRange(true)
                }
                chartPanel?.repaint()
            }
        }
    }

    override fun close() {
        // Force final update before closing
        forceUpdate()

        applicationFrame?.dispose()

        println("Chart rendering completed.")
        println("Price data points: ${priceTimeSeries.itemCount}")
        println("Buy signals: ${buySignals.size}")
        println("Sell signals: ${sellSignals.size}")
    }
}

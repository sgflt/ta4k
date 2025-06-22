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

import java.time.Instant
import org.ta4j.core.api.Indicators
import org.ta4j.core.api.callback.MarketEventHandler
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContext.IndicatorIdentification
import org.ta4j.core.indicators.IndicatorContextUpdateListener
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.strategy.DefaultStrategy
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.Strategy
import org.ta4j.core.strategy.StrategyFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.live.LiveTrading
import org.ta4j.core.trading.live.LiveTradingBuilder
import org.ta4j.core.trading.preparation.ObservableIndicatorCalculationBuilder
import org.ta4j.core.trading.signal.BuySignal
import org.ta4j.core.trading.signal.ObservableStrategyFactoryBuilder
import org.ta4j.core.trading.signal.SellSignal
import org.ta4j.core.trading.signal.Signal
import org.ta4j.core.trading.signal.SignalListener
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Simplified chart class for buy/sell signals using ObservableStrategy.
 *
 * Uses ChartRenderer for thread-safe chart operations and focuses on
 * signal detection and indicator management.
 */
class BuyAndSellSignalsToChart(
    private val timeFrame: TimeFrame = TimeFrame.DAY,
    private val enableLiveUpdates: Boolean = false,
) : IndicatorContextUpdateListener, SignalListener, AutoCloseable {

    private val indicatorContexts: IndicatorContexts
    private val marketEventHandler: MarketEventHandler
    private lateinit var liveTrading: LiveTrading
    private val chartRenderer = ChartRenderer(enableLiveUpdates)
    
    @Volatile
    private var lastUpdateTime: Instant = Instant.MIN

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

    fun setUpStrategy(strategyFactory: StrategyFactory<Strategy>) {
        val observableFactory = ObservableStrategyFactoryBuilder()
            .withSignalListener(this)
            .withStrategyFactory(strategyFactory)
            .build()

        liveTrading = LiveTradingBuilder()
            .withStrategyFactory(observableFactory)
            .withIndicatorContexts(indicatorContexts)
            .withName("BuyAndSellSignalsChart")
            .build()
    }

    override fun onContextUpdate(time: Instant) {
        if (time.isAfter(lastUpdateTime)) {
            lastUpdateTime = time
            updatePriceData(time)
        }
    }

    override fun onSignal(signal: Signal) {
        val context = indicatorContexts[timeFrame]
        val currentPrice = context.getNumericIndicator(CLOSE_PRICE)?.value?.doubleValue() ?: return

        if (!currentPrice.isFinite() || currentPrice <= 0) {
            println("Invalid price data: $currentPrice, skipping signal")
            return
        }

        when (signal) {
            is BuySignal -> {
                chartRenderer.addBuySignal(signal.whenReceived, currentPrice)
                println("BUY Signal at ${signal.whenReceived} - Price: $currentPrice")
            }
            is SellSignal -> {
                chartRenderer.addSellSignal(signal.whenReceived, currentPrice)
                println("SELL Signal at ${signal.whenReceived} - Price: $currentPrice")
            }
        }
    }

    private fun updatePriceData(time: Instant) {
        val context = indicatorContexts[timeFrame]
        context.getNumericIndicator(CLOSE_PRICE)?.value?.doubleValue()?.let { closePrice ->
            if (closePrice.isFinite() && closePrice > 0) {
                chartRenderer.addPriceData(time, closePrice)
            }
        }
    }

    fun processMarketEvent(marketEvent: CandleReceived) {
        marketEventHandler.onCandle(marketEvent)
    }

    fun processEventsWithProgress(marketEvents: List<CandleReceived>, showProgress: Boolean = true, delayMs: Long = 0) {
        if (showProgress) {
            println("Processing ${marketEvents.size} market events with live signal detection...")
            if (delayMs > 0) {
                println("Using ${delayMs}ms delay between events for live visualization")
            }
        }

        marketEvents.forEachIndexed { index, marketEvent ->
            processMarketEvent(marketEvent)

            // Add delay for live visualization if requested
            if (delayMs > 0) {
                Thread.sleep(delayMs)
            }

            if (showProgress && (index + 1) % 20 == 0) {
                println("Processed ${index + 1}/${marketEvents.size} events - Buy: ${chartRenderer.getBuySignalsCount()}, Sell: ${chartRenderer.getSellSignalsCount()}")
            }
        }

        if (showProgress) {
            println("Completed processing all ${marketEvents.size} market events.")
            println("Total signals detected - Buy: ${chartRenderer.getBuySignalsCount()}, Sell: ${chartRenderer.getSellSignalsCount()}")
        }
    }

    fun createChart() = chartRenderer.createChart()

    fun displayChart() {
        chartRenderer.displayChart()
        if (enableLiveUpdates) {
            println("Live signal chart updates enabled.")
        }
    }

    // Getter methods for testing
    fun getClosePriceSeries() = chartRenderer.getPriceSeriesCopy()
    fun getBuySignals() = chartRenderer.getBuySignals().map { it.time to it.price }
    fun getSellSignals() = chartRenderer.getSellSignals().map { it.time to it.price }
    
    // Force chart update
    fun forceChartUpdate() = chartRenderer.forceUpdate()

    override fun close() {
        chartRenderer.close()
    }

    companion object {
        // Indicator identifications for type-safe access
        private val CLOSE_PRICE = IndicatorIdentification("closePrice")

        // Helper method to create a strategy factory for demonstration with ObservableStrategyFactoryBuilder
        private fun createMovingMomentumStrategyFactory(): StrategyFactory<Strategy> {
            return object : StrategyFactory<Strategy> {
                override val tradeType = org.ta4j.core.TradeType.BUY

                override fun createStrategy(
                    configuration: StrategyConfiguration,
                    runtimeContext: RuntimeContext,
                    indicatorContexts: IndicatorContexts,
                ): Strategy {
                    val indicatorContext = indicatorContexts[TimeFrame.DAY]

                    val closePrice = Indicators.closePrice()
                    val shortEma = closePrice.ema(9)
                    val longEma = closePrice.ema(26)

                    // Create entry and exit rules
                    val entryCross = shortEma.crossedOver(longEma)
                    val exitCross = shortEma.crossedUnder(longEma)

                    // IMPORTANT: Register the close price indicator that the chart needs
                    indicatorContext.add(closePrice, IndicatorIdentification("closePrice"))
                    indicatorContext.add(shortEma, IndicatorIdentification("shortEma"))
                    indicatorContext.add(longEma, IndicatorIdentification("longEma"))
                    indicatorContext.add(entryCross, IndicatorIdentification("entryCross"))
                    indicatorContext.add(exitCross, IndicatorIdentification("exitCross"))

                    return DefaultStrategy(
                        name = "Moving Momentum Strategy",
                        timeFrames = setOf(TimeFrame.DAY),
                        entryRule = entryCross.toRule(),
                        exitRule = exitCross.toRule(),
                        indicatorContext = indicatorContext
                    )
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println("Starting Buy and Sell Signals to Chart visualization using ObservableStrategy...")

            // Get market data
            val marketEvents = MockMarketEventsLoader.loadMarketEvents(1000)

            // Check user mode selection
            val mode = if (args.isNotEmpty()) args[0].lowercase() else "static"

            when (mode) {
                "live" -> {
                    println("=== LIVE TRADING BUILDER MODE ===")
                    // Create chart generator using LiveTradingBuilder approach
                    BuyAndSellSignalsToChart(enableLiveUpdates = true).use { signalChart ->
                        // Setup strategy using factory approach with LiveTradingBuilder
                        val strategyFactory = createMovingMomentumStrategyFactory()
                        signalChart.setUpStrategy(strategyFactory)

                        // Display chart first (empty)
                        signalChart.displayChart()

                        // Process events with progress - signals will appear in real-time with delay
                        // Use 50ms delay between events for visual effect (adjust as needed)
                        signalChart.processEventsWithProgress(marketEvents, showProgress = true, delayMs = 50)
                        
                        // Force final chart update to ensure all data is visible
                        signalChart.forceChartUpdate()

                        println("LiveTradingBuilder-based signal detection completed!")
                        println("Final stats - Buy: ${signalChart.getBuySignals().size}, Sell: ${signalChart.getSellSignals().size}")
                        println("Press Enter to continue...")
                        readLine()
                    }
                }

                else -> {
                    println("=== STATIC MODE ===")
                    // Create chart generator with standard mode
                    BuyAndSellSignalsToChart().use { signalChart ->
                        // Setup strategy
                        signalChart.setUpStrategy(createMovingMomentumStrategyFactory())

                        // Process all market events first
                        println("Processing ${marketEvents.size} market events...")
                        marketEvents.forEach { marketEvent ->
                            signalChart.processMarketEvent(marketEvent)
                        }

                        // Display the chart with all signals
                        val priceDataPoints = signalChart.getClosePriceSeries().itemCount
                        val totalSignals = signalChart.getBuySignals().size + signalChart.getSellSignals().size
                        println("Displaying chart with $priceDataPoints data points and $totalSignals signals...")
                        
                        if (priceDataPoints == 0) {
                            println("WARNING: No price data was processed! Check indicator setup.")
                        }
                        
                        signalChart.displayChart()
                    }
                }
            }

            println("Signal chart visualization completed!")
            println("Available modes:")
            println("  - 'static' (default): Process all data then show chart")
            println("  - 'live': Show empty chart with real-time signal updates")
        }
    }
}

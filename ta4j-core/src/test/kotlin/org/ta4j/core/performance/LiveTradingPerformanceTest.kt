/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.performance

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.DoubleEMAIndicator
import org.ta4j.core.indicators.numeric.average.EMAIndicator
import org.ta4j.core.indicators.numeric.average.HMAIndicator
import org.ta4j.core.indicators.numeric.average.LWMAIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.average.TripleEMAIndicator
import org.ta4j.core.indicators.numeric.average.WMAIndicator
import org.ta4j.core.indicators.numeric.average.ZLEMAIndicator
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.MedianPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.OpenPriceIndicator
import org.ta4j.core.indicators.numeric.candles.price.TypicalPriceIndicator
import org.ta4j.core.indicators.numeric.momentum.ROCIndicator
import org.ta4j.core.indicators.numeric.momentum.RSIIndicator
import org.ta4j.core.indicators.numeric.oscillators.CMOIndicator
import org.ta4j.core.indicators.numeric.oscillators.DPOIndicator
import org.ta4j.core.indicators.numeric.oscillators.MACDIndicator
import org.ta4j.core.indicators.numeric.statistics.CorrelationCoefficientIndicator
import org.ta4j.core.indicators.numeric.statistics.CovarianceIndicator
import org.ta4j.core.indicators.numeric.statistics.MeanDeviationIndicator
import org.ta4j.core.indicators.numeric.statistics.SigmaIndicator
import org.ta4j.core.indicators.numeric.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardErrorIndicator
import org.ta4j.core.indicators.numeric.statistics.VarianceIndicator
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.configuration.StrategyConfiguration
import org.ta4j.core.trading.live.LiveTrading
import org.ta4j.core.trading.live.LiveTradingBuilder

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
open class LiveTradingPerformanceTest {

    private lateinit var liveTrading: LiveTrading
    private lateinit var candleEvents: List<CandleReceived>
    private lateinit var indicators: List<NumericIndicator>
    
    @Setup
    fun setup() {
        val barLength = 200
        val numFactory = DoubleNumFactory
        val timeFrame = TimeFrame.MINUTES_1
        
        // Create indicator contexts
        val indicatorContext = IndicatorContext.empty(timeFrame)
        val indicatorContexts = IndicatorContexts.empty()
        indicatorContexts.add(indicatorContext)
        
        // Create configuration
        val configuration = StrategyConfiguration()
        
        // Build LiveTrading using LiveTradingBuilder
        liveTrading = LiveTradingBuilder()
            .withNumFactory(numFactory)
            .withName("PerformanceTest")
            .withIndicatorContexts(indicatorContexts)
            .withConfiguration(configuration)
            .build()
        
        // Generate candle events for simulation
        candleEvents = generateCandleEvents(1000, timeFrame)
        
        // Setup indicators using MarketEventTestContext for proper initialization
        setupIndicators(barLength, numFactory)
    }
    
    private fun generateCandleEvents(count: Int, timeFrame: TimeFrame): List<CandleReceived> {
        val events = mutableListOf<CandleReceived>()
        // Start from a fixed time in the past to avoid timing conflicts
        var baseTime = Instant.parse("2024-01-01T00:00:00Z")
        
        for (i in 0 until count) {
            val open = 100.0 + Random.nextDouble(-5.0, 5.0)
            val high = open + Random.nextDouble(0.0, 3.0)
            val low = open - Random.nextDouble(0.0, 3.0)
            val close = low + Random.nextDouble(0.0, high - low)
            val volume = 1000.0 + Random.nextDouble(0.0, 2000.0)
            
            events.add(
                CandleReceived(
                    timeFrame = timeFrame,
                    beginTime = baseTime,
                    endTime = baseTime.plus(Duration.ofMinutes(1)),
                    openPrice = open,
                    highPrice = high,
                    lowPrice = low,
                    closePrice = close,
                    volume = volume
                )
            )
            baseTime = baseTime.plus(Duration.ofMinutes(1))
        }
        return events
    }
    
    private fun setupIndicators(barLength: Int, numFactory: DoubleNumFactory) {
        // Use MarketEventTestContext to properly create indicators
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withDefaultMarketEvents()
        
        val series = context.barSeries
        
        // Create basic price indicators - using correct constructors
        val closePrice = ClosePriceIndicator(numFactory)
        val highPrice = HighPriceIndicator(numFactory)
        val lowPrice = LowPriceIndicator(numFactory)
        val openPrice = OpenPriceIndicator(numFactory)
        val typicalPrice = TypicalPriceIndicator(numFactory)
        val medianPrice = MedianPriceIndicator(numFactory)
        
        // Create a selection of working indicators with barLength 200
        indicators = listOf(
            // Price indicators
            closePrice,
            highPrice,
            lowPrice,
            openPrice,
            typicalPrice,
            medianPrice,
            
            // Moving averages - these are known to work
            SMAIndicator(closePrice, barLength),
            EMAIndicator(closePrice, barLength),
            WMAIndicator(closePrice, barLength),
            LWMAIndicator(closePrice, barLength),
            HMAIndicator(closePrice, barLength),
            MMAIndicator(closePrice, barLength),
            DoubleEMAIndicator(closePrice, barLength),
            TripleEMAIndicator(closePrice, barLength),
            ZLEMAIndicator(closePrice, barLength),
            
            // Momentum indicators
            RSIIndicator(closePrice, barLength),
            ROCIndicator(numFactory, closePrice, barLength),
            
            // Oscillators
            MACDIndicator(numFactory, closePrice, 12, 26),
            CMOIndicator(numFactory, closePrice, barLength),
            DPOIndicator(numFactory, closePrice, barLength),
            
            // Statistics
            StandardDeviationIndicator(closePrice, barLength),
            VarianceIndicator(closePrice, barLength),
            StandardErrorIndicator(closePrice, barLength),
            MeanDeviationIndicator(closePrice, barLength),
            SigmaIndicator(closePrice, barLength),
            CovarianceIndicator(closePrice, highPrice, barLength),
            CorrelationCoefficientIndicator(closePrice, highPrice, barLength),
            SimpleLinearRegressionIndicator(closePrice, barLength)
        )
        
        // Add indicators to test context for proper initialization
        indicators.forEach { indicator ->
            context.withIndicator(indicator)
        }
        
        // Fast forward to populate some initial data
        context.fastForward(50)
    }
    

    @Benchmark
    fun benchmarkLiveTrading(): Int {
        // Create fresh instance for each benchmark to avoid time conflicts
        val freshLiveTrading = createFreshLiveTrading()
        var processedEvents = 0
        
        for (candleEvent in candleEvents) {
            freshLiveTrading.onCandle(candleEvent)
            processedEvents++
        }
        
        return processedEvents
    }
    
    private fun createFreshLiveTrading(): LiveTrading {
        val numFactory = DoubleNumFactory
        val timeFrame = TimeFrame.MINUTES_1
        
        val indicatorContext = IndicatorContext.empty(timeFrame)
        val indicatorContexts = IndicatorContexts.empty()
        indicatorContexts.add(indicatorContext)
        
        val configuration = StrategyConfiguration()
        
        return LiveTradingBuilder()
            .withNumFactory(numFactory)
            .withName("PerformanceTestFresh")
            .withIndicatorContexts(indicatorContexts)
            .withConfiguration(configuration)
            .build()
    }
}

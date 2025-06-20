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
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.IndicatorContext
import org.ta4j.core.indicators.IndicatorContexts
import org.ta4j.core.indicators.TimeFrame
import org.ta4j.core.indicators.numeric.NumericIndicator
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
        
        // Create indicators using Indicators factory and fluent API
        val factory = Indicators.extended(numFactory)
        
        // Basic price indicators using factory methods
        val closePrice = factory.closePrice()
        val highPrice = factory.highPrice()
        val lowPrice = factory.lowPrice()
        val openPrice = factory.openPrice()
        val typicalPrice = factory.typicalPrice()
        val medianPrice = factory.medianPrice()
        val volume = factory.volume()
        
        // Create comprehensive indicator set using fluent API and factory methods
        indicators = listOf(
            // Price indicators
            closePrice,
            highPrice,
            lowPrice,
            openPrice,
            typicalPrice,
            medianPrice,
            volume,
            
            // Moving averages using fluent API
            closePrice.sma(barLength),
            closePrice.ema(barLength),
            closePrice.wma(barLength),
            closePrice.lwma(barLength),
            closePrice.hma(barLength),
            closePrice.mma(barLength),
            closePrice.doubleEMA(barLength),
            closePrice.tripleEma(barLength),
            closePrice.zlema(barLength),
            
            // Momentum indicators using fluent API
            closePrice.rsi(barLength),
            closePrice.roc(barLength),
            
            // Oscillators using factory and fluent API
            closePrice.macd(12, 26),
            closePrice.cmo(barLength),
            closePrice.dpo(barLength),
            
            // Statistics using fluent API
            closePrice.stddev(barLength),
            closePrice.variance(barLength),
            closePrice.stderr(barLength),
            closePrice.meanDeviation(barLength),
            closePrice.sigma(barLength),
            closePrice.covariance(highPrice, barLength),
            closePrice.simpleLinearRegression(barLength),
            
            // Volume indicators using factory methods
            factory.obv(),
            factory.accDist(),
            factory.mfi(barLength),
            factory.pvi(),
            factory.vwap(barLength),
            factory.mvwap(barLength, barLength),
            factory.chaikinMoneyFlow(barLength),
            factory.chaikinOscillator(3, 10),
            factory.iii(),
            factory.pvo(barLength, 12, 26),
            factory.rocv(barLength),
            
            // Technical indicators using factory methods
            factory.williamsR(barLength),
            factory.massIndex(25, barLength),
            factory.cci(barLength),
            factory.superTrend(barLength, 3.0),
            factory.parabolicSAR(),
            factory.chop(barLength),
            factory.fisher(barLength),
            factory.atr(barLength),
            factory.adx(barLength),
            factory.plusDMI(),
            factory.minusDMI(),
            factory.plusDII(barLength),
            factory.minusDII(barLength),
            factory.aroonUp(barLength),
            factory.aroonDown(barLength),
            factory.aroonOscillator(barLength),
            factory.stochasticKOscillator(barLength),
            factory.stochasticOscillator(barLength),
            factory.awesomeOscillator(5, 34),
            factory.rwiHigh(barLength),
            factory.rwiLow(barLength),
            
            // Candle indicators
            factory.realBody(),
            factory.lowerShadow(),
            factory.upperShadow(),
            factory.closeLocationValue(),
            
            // Helper indicators using fluent API
            closePrice.highest(barLength),
            closePrice.lowest(barLength),
            closePrice.runningTotal(barLength),
            closePrice.gain(),
            closePrice.loss(),
            closePrice.distance(),
            closePrice.ulcer(barLength),
            closePrice.ravi(7, 65),
            
            // Channel indicators using factory methods
            factory.bollingerBands(barLength, 2.0).upper,
            factory.bollingerBands(barLength, 2.0).middle,
            factory.bollingerBands(barLength, 2.0).lower,
            factory.donchianChannel(barLength).upper,
            factory.donchianChannel(barLength).middle,
            factory.donchianChannel(barLength).lower
        )
        
        // Add indicators to test context for proper initialization
        indicators.forEach { indicator ->
            context.withIndicator(indicator)
        }
        
        // Fast forward to populate initial data
        context.fastForwardUntilStable()
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

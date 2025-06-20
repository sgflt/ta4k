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
package ta4jexamples.strategies.ai

import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame

/**
 * Generates realistic mock market data for testing LiveTrading functionality
 */
class MockMarketDataGenerator(
    private val timeFrame: TimeFrame,
    initialPrice: Double = 45000.0
) {
    private var currentPrice = initialPrice
    private var currentTime = Instant.now()
    private var trendDirection = 1.0 // 1.0 for uptrend, -1.0 for downtrend
    private var trendStrength = 0.5 // 0.0 to 1.0
    private var volatility = 0.02 // Base volatility
    private val random = Random.Default
    
    /**
     * Generates the next candle with realistic price movements
     */
    fun generateNextCandle(): CandleReceived {
        // Update trend occasionally
        if (random.nextDouble() < 0.05) { // 5% chance to change trend
            trendDirection *= -1
            trendStrength += random.nextDouble(-0.3, 0.3)
        }
        
        // Adjust volatility based on market conditions
        volatility = when {
            random.nextDouble() < 0.1 -> random.nextDouble(0.03, 0.04) // High volatility
            random.nextDouble() < 0.2 -> random.nextDouble(0.008, 0.012) // Low volatility
            else -> random.nextDouble(0.015, 0.02) // Normal volatility
        }
        
        val openPrice = currentPrice
        
        // Generate price movement with trend bias
        val trendMove = trendDirection * trendStrength * random.nextDouble(0.001, 0.003)
        val randomMove = (random.nextDouble(-1.0, 1.0) * volatility)
        val priceChange = trendMove + randomMove
        
        val closePrice = openPrice * (1 + priceChange)
        
        // Generate high and low prices
        val volatilityFactor = volatility * random.nextDouble(0.5, 2.0)
        val highPrice = max(openPrice, closePrice) * (1 + volatilityFactor * random.nextDouble())
        val lowPrice = min(openPrice, closePrice) * (1 - volatilityFactor * random.nextDouble())
        
        // Generate volume with some correlation to price movement
        val baseVolume = 100.0
        val volumeMultiplier = 1.0 + abs(priceChange) * 50 + random.nextDouble(0.5, 2.0)
        val volume = baseVolume * volumeMultiplier
        
        currentPrice = closePrice
        currentTime = currentTime.plus(Duration.ofMinutes(5))
        
        return CandleReceived(
            timeFrame = timeFrame,
            beginTime = currentTime.minus(Duration.ofMinutes(5)),
            endTime = currentTime,
            openPrice = openPrice,
            highPrice = highPrice,
            lowPrice = lowPrice,
            closePrice = closePrice,
            volume = volume
        )
    }
    
    /**
     * Generates a sequence of candles
     */
    fun generateCandles(count: Int): List<CandleReceived> {
        return (1..count).map { generateNextCandle() }
    }
    
    /**
     * Simulates a market crash scenario
     */
    fun simulateMarketCrash(): CandleReceived {
        val openPrice = currentPrice
        val crashPercent = random.nextDouble(0.05, 0.15) // 5-15% drop
        val closePrice = openPrice * (1 - crashPercent)
        
        val highPrice = openPrice * (1 + random.nextDouble(0.01, 0.02))
        val lowPrice = closePrice * (1 - random.nextDouble(0.01, 0.03))
        
        val volume = 1000.0 * random.nextDouble(2.0, 5.0) // High volume during crash
        
        currentPrice = closePrice
        currentTime = currentTime.plus(Duration.ofMinutes(5))
        trendDirection = -1.0
        trendStrength = 0.8
        volatility = 0.08
        
        return CandleReceived(
            timeFrame = timeFrame,
            beginTime = currentTime.minus(Duration.ofMinutes(5)),
            endTime = currentTime,
            openPrice = openPrice,
            highPrice = highPrice,
            lowPrice = lowPrice,
            closePrice = closePrice,
            volume = volume
        )
    }
    
    /**
     * Simulates a market pump scenario
     */
    fun simulateMarketPump(): CandleReceived {
        val openPrice = currentPrice
        val pumpPercent = random.nextDouble(0.08, 0.20) // 8-20% gain
        val closePrice = openPrice * (1 + pumpPercent)
        
        val lowPrice = openPrice * (1 - random.nextDouble(0.01, 0.02))
        val highPrice = closePrice * (1 + random.nextDouble(0.01, 0.03))
        
        val volume = 1000.0 * random.nextDouble(3.0, 8.0) // Very high volume during pump
        
        currentPrice = closePrice
        currentTime = currentTime.plus(Duration.ofMinutes(5))
        trendDirection = 1.0
        trendStrength = 0.9
        volatility = 0.06
        
        return CandleReceived(
            timeFrame = timeFrame,
            beginTime = currentTime.minus(Duration.ofMinutes(5)),
            endTime = currentTime,
            openPrice = openPrice,
            highPrice = highPrice,
            lowPrice = lowPrice,
            closePrice = closePrice,
            volume = volume
        )
    }
    
    /**
     * Gets current market conditions summary
     */
    fun getMarketSummary(): String {
        val trend = if (trendDirection > 0) "BULLISH" else "BEARISH"
        val vol = when {
            volatility > 0.05 -> "HIGH"
            volatility < 0.015 -> "LOW"
            else -> "NORMAL"
        }
        return "Market: $trend trend (strength: ${String.format("%.1f", trendStrength * 100)}%), " +
               "Volatility: $vol (${String.format("%.2f", volatility * 100)}%), " +
               "Price: ${String.format("%.2f", currentPrice)}"
    }
}

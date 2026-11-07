/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies.sma

import java.time.Duration
import java.time.Instant
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame

class SimpleMarketDataGenerator(
    private val timeFrame: TimeFrame,
    initialPrice: Double = 100.0
) {
    private var currentPrice = initialPrice
    private var currentTime = Instant.now()
    private val random = Random.Default
    
    fun generateNextCandle(): CandleReceived {
        val openPrice = currentPrice
        val priceChange = random.nextDouble(-0.02, 0.02)
        val closePrice = openPrice * (1 + priceChange)
        
        val highPrice = max(openPrice, closePrice) * (1 + random.nextDouble(0.0, 0.01))
        val lowPrice = min(openPrice, closePrice) * (1 - random.nextDouble(0.0, 0.01))
        val volume = random.nextDouble(100.0, 1000.0)
        
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
    
    fun generateCandles(count: Int): List<CandleReceived> {
        return (1..count).map { generateNextCandle() }
    }
}

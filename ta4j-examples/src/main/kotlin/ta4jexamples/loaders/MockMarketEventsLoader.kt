/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.loaders

import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.TimeFrame

/**
 * Mock market events for modern LiveTrading examples.
 * Creates mock market events that can be fed to LiveTrading systems.
 */
object MockMarketEventsLoader {

    /**
     * Load market events for modern LiveTrading approach.
     * Returns a list of CandleReceived events that can be fed to LiveTrading systems.
     */
    fun loadMarketEvents(count: Int = 50): List<CandleReceived> {
        val baseTime = Instant.parse("2023-01-01T00:00:00Z")
        val duration = Duration.ofDays(1)

        var previousPrice = 800.0
        val volatility = 20.0

        return (0 until count).map { i ->
            val startTime = baseTime.plus(duration.multipliedBy(i.toLong()))
            val endTime = startTime.plus(duration)

            // Create realistic price movements with trend and volatility
            val basePrice = previousPrice + Random.nextDouble() * 10 - 5.0
            val openPrice = basePrice + (Random.nextDouble() - 0.5) * volatility
            val closePrice = openPrice + (Random.nextDouble() - 0.5) * volatility
            val highPrice = maxOf(openPrice, closePrice) + Random.nextDouble() * (volatility / 2)
            val lowPrice = minOf(openPrice, closePrice) - Random.nextDouble() * (volatility / 2)
            val volume = 1000.0 + Random.nextDouble() * 5000.0

            previousPrice = closePrice
            
            CandleReceived(
                timeFrame = TimeFrame.DAY,
                beginTime = startTime,
                endTime = endTime,
                openPrice = openPrice,
                highPrice = highPrice,
                lowPrice = lowPrice,
                closePrice = closePrice,
                volume = volume
            )
        }
    }
}

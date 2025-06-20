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

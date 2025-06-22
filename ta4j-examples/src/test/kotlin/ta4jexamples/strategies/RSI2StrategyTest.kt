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
package ta4jexamples.strategies

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.configuration.ParameterName
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Test for the migrated RSI2Strategy from Java to Kotlin.
 */
class RSI2StrategyTest {

    @Test
    fun testRSI2StrategyExecution() {
        // Test that the strategy executes without exceptions
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyRun = RSI2StrategyRun()
        
        val backtestExecutor = BacktestExecutorBuilder()
            .numFactory(DoubleNumFactory)
            .build()
            
        val statement = backtestExecutor.execute(strategyRun, marketEvents, 1000.0)
        val tradingRecord = statement.strategy.tradeRecord
        
        // Verify basic results
        assertNotNull(tradingRecord)
        assertTrue(tradingRecord.positionCount >= 0)
    }
    
    @Test
    fun testRSI2StrategyMain() {
        // Test that the main method runs without exceptions
        assertDoesNotThrow {
            RSI2Strategy.main(emptyArray())
        }
    }
    
    @Test
    fun testRSI2StrategyConfiguration() {
        val strategyRun = RSI2StrategyRun()
        
        // Test default configuration values
        // RSI configuration
        assertEquals(2, strategyRun.configuration.getInt(ParameterName("rsiPeriod")))
        assertEquals(5, strategyRun.configuration.getInt(ParameterName("rsiOversoldThreshold")))
        assertEquals(95, strategyRun.configuration.getInt(ParameterName("rsiOverboughtThreshold")))
        
        // SMA periods for trend filtering
        assertEquals(5, strategyRun.configuration.getInt(ParameterName("shortSmaPeriod")))
        assertEquals(200, strategyRun.configuration.getInt(ParameterName("longSmaPeriod")))
    }
}
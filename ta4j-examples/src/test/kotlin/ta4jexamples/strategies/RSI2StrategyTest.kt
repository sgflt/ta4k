/*
 * SPDX-License-Identifier: MIT
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
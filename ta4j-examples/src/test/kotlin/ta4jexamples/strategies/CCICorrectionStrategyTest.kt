/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.num.DoubleNumFactory
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Test for the migrated CCICorrectionStrategy from Java to Kotlin.
 */
class CCICorrectionStrategyTest {

    @Test
    fun testCCICorrectionStrategyExecution() {
        // Test that the strategy executes without exceptions
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyRun = CCICorrectionStrategyRun()
        
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
    fun testCCICorrectionStrategyMain() {
        // Test that the main method runs without exceptions
        assertDoesNotThrow {
            CCICorrectionStrategy.main(emptyArray())
        }
    }
    
    @Test
    fun testCCICorrectionStrategyConfiguration() {
        val strategyRun = CCICorrectionStrategyRun()
        
        // Test default configuration values
        assertEquals(200, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("longCciPeriod")))
        assertEquals(5, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("shortCciPeriod")))
        assertEquals(100, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("upperThreshold")))
        assertEquals(-100, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("lowerThreshold")))
    }
}
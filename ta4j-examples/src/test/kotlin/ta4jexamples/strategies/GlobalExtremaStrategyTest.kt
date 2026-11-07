/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.strategies

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.ta4j.core.backtest.BacktestExecutorBuilder
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.strategy.configuration.ParameterName
import ta4jexamples.loaders.MockMarketEventsLoader

/**
 * Test for the migrated GlobalExtremaStrategy from Java to Kotlin.
 */
class GlobalExtremaStrategyTest {

    @Test
    fun testGlobalExtremaStrategyExecution() {
        // Test that the strategy executes without exceptions
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyRun = GlobalExtremaStrategyRun()
        
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
    fun testGlobalExtremaStrategyMain() {
        // Test that the main method runs without exceptions
        assertDoesNotThrow {
            GlobalExtremaStrategy.main(emptyArray())
        }
    }
    
    @Test
    fun testGlobalExtremaStrategyConfiguration() {
        val strategyRun = GlobalExtremaStrategyRun()
        
        // Test default configuration values
        assertEquals(2016, strategyRun.configuration.getInt(ParameterName("barsPerWeek")))
        assertEquals(1.004, strategyRun.configuration.getDouble(ParameterName("lowMultiplier")))
        assertEquals(0.996, strategyRun.configuration.getDouble(ParameterName("highMultiplier")))
    }
}

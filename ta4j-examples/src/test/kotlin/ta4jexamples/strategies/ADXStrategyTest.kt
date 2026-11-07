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
 * Test for the migrated ADXStrategy from Java to Kotlin.
 */
class ADXStrategyTest {

    @Test
    fun testADXStrategyExecution() {
        // Test that the strategy executes without exceptions
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyRun = ADXStrategyRun()
        
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
    fun testADXStrategyMain() {
        // Test that the main method runs without exceptions
        assertDoesNotThrow {
            ADXStrategy.main(emptyArray())
        }
    }
    
    @Test
    fun testADXStrategyConfiguration() {
        val strategyRun = ADXStrategyRun()
        
        // Test default configuration values
        assertEquals(14, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("adxPeriod")))
        assertEquals(50, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("smaPeriod")))
        assertEquals(20, strategyRun.configuration.getInt(org.ta4j.core.strategy.configuration.ParameterName("adxThreshold")))
    }
}
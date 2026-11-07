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
 * Test for the migrated MovingMomentumStrategy from Java to Kotlin.
 */
class MovingMomentumStrategyTest {

    @Test
    fun testMovingMomentumStrategyExecution() {
        // Test that the strategy executes without exceptions
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()
        val strategyRun = MovingMomentumStrategyRun()
        
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
    fun testMovingMomentumStrategyMain() {
        // Test that the main method runs without exceptions
        assertDoesNotThrow {
            MovingMomentumStrategy.main(emptyArray())
        }
    }
    
    @Test
    fun testMovingMomentumStrategyConfiguration() {
        val strategyRun = MovingMomentumStrategyRun()
        
        // Test default configuration values
        // EMA periods
        assertEquals(9, strategyRun.configuration.getInt(ParameterName("shortEma")))
        assertEquals(26, strategyRun.configuration.getInt(ParameterName("longEma")))
        
        // MACD periods  
        assertEquals(9, strategyRun.configuration.getInt(ParameterName("macdFast")))
        assertEquals(26, strategyRun.configuration.getInt(ParameterName("macdSlow")))
        assertEquals(18, strategyRun.configuration.getInt(ParameterName("macdSignal")))
        
        // Stochastic configuration
        assertEquals(14, strategyRun.configuration.getInt(ParameterName("stochasticPeriod")))
        assertEquals(20, strategyRun.configuration.getInt(ParameterName("stochasticLowThreshold")))
        assertEquals(80, strategyRun.configuration.getInt(ParameterName("stochasticHighThreshold")))
    }
}
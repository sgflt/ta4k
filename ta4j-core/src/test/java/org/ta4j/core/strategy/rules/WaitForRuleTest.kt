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
package org.ta4j.core.strategy.rules

import java.time.Instant
import java.time.temporal.ChronoUnit
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.ta4j.core.TradeType
import org.ta4j.core.TradingRecordTestContext
import org.ta4j.core.strategy.RuntimeContext
import org.ta4j.core.strategy.RuntimeValueResolver

class WaitForRuleTest {

    @Test
    fun `should throw exception for negative wait period`() {
        val mockContext = mockk<RuntimeContext>()

        assertThatThrownBy {
            WaitForRule(
                tradeType = TradeType.BUY,
                waitPeriod = -1,
                timeUnit = ChronoUnit.DAYS,
                runtimeContext = mockContext
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Wait period must be non-negative")
    }

    @Test
    fun `should not be satisfied when no last trade time available`() {
        val currentTime = Instant.parse("2024-01-10T12:00:00Z")

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns null

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should not be satisfied when no current time available`() {
        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns null

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should not be satisfied when insufficient time has passed since last trade`() {
        val lastTradeTime = Instant.parse("2024-01-08T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:00:00Z") // Only 2 days have passed

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isFalse()
    }

    @Test
    fun `should be satisfied when exactly enough time has passed since last trade`() {
        val lastTradeTime = Instant.parse("2024-01-05T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:00:00Z") // Exactly 5 days have passed

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should be satisfied when more than enough time has passed since last trade`() {
        val lastTradeTime = Instant.parse("2024-01-03T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:00:00Z") // 7 days have passed

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should handle different trade types correctly`() {
        val lastBuyTime = Instant.parse("2024-01-03T12:00:00Z")  // 7 days ago
        val lastSellTime = Instant.parse("2024-01-07T12:00:00Z") // 3 days ago
        val currentTime = Instant.parse("2024-01-10T12:00:00Z")

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastBuyTime
        every { mockContext.getValue("lastTradeSELLTime") } returns lastSellTime

        val buyRule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        val sellRule = WaitForRule(
            tradeType = TradeType.SELL,
            waitPeriod = 5,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(buyRule.isSatisfied).isTrue()  // 7 days >= 5 days
        assertThat(sellRule.isSatisfied).isFalse() // 3 days < 5 days
    }

    @Test
    fun `should be satisfied immediately when zero wait period required`() {
        val tradeTime = Instant.parse("2024-01-10T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:00:00Z") // Same time

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns tradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 0,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should work with different time units - hours`() {
        val lastTradeTime = Instant.parse("2024-01-10T08:00:00Z")
        val currentTime = Instant.parse("2024-01-10T14:00:00Z") // 6 hours later

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 5,
            timeUnit = ChronoUnit.HOURS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue() // 6 hours >= 5 hours
    }

    @Test
    fun `should work with different time units - minutes`() {
        val lastTradeTime = Instant.parse("2024-01-10T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:07:00Z") // 7 minutes later

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 10,
            timeUnit = ChronoUnit.MINUTES,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isFalse() // 7 minutes < 10 minutes
    }

    @Test
    fun `should work with trading record test context integration`() {
        val context = TradingRecordTestContext()
            .withTradeType(TradeType.BUY)

        // Simulate trading activity with specific times
        val baseTime = Instant.parse("2024-01-01T12:00:00Z")

        context.enter(100.0).at(50.0)
        baseTime

        context.forwardTime(3)
        context.exit(100.0).at(55.0)
        val exitTime = baseTime.plus(3, ChronoUnit.DAYS)

        context.forwardTime(2)
        val currentTime = baseTime.plus(5, ChronoUnit.DAYS)

        // Create a mock runtime context
        val mockRuntimeContext = mockk<RuntimeContext>()
        every { mockRuntimeContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockRuntimeContext)
        }
        every { mockRuntimeContext.getValue("currentTime") } returns currentTime
        every { mockRuntimeContext.getValue("lastTradeSELLTime") } returns exitTime

        val rule = WaitForRule(
            tradeType = TradeType.SELL, // Wait for SELL (exit) trades
            waitPeriod = 3,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockRuntimeContext
        )

        // Should not be satisfied as only 2 days have passed since SELL
        assertThat(rule.isSatisfied).isFalse()

        // Update current time to 3 days after exit
        val laterTime = exitTime.plus(3, ChronoUnit.DAYS)
        every { mockRuntimeContext.getValue("currentTime") } returns laterTime

        // Now should be satisfied as 3 days have passed since SELL
        assertThat(rule.isSatisfied).isTrue()
    }

    @Test
    fun `should handle edge case when current time equals last trade time`() {
        val tradeTime = Instant.parse("2024-01-10T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:00:00Z") // Same time

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns tradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 1,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isFalse() // 0 time has passed, need 1 day
    }

    @Test
    fun `should have descriptive string representation`() {
        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns null

        val rule = WaitForRule(
            tradeType = TradeType.SELL,
            waitPeriod = 7,
            timeUnit = ChronoUnit.HOURS,
            runtimeContext = mockContext
        )

        val stringRep = rule.toString()
        assertThat(stringRep).contains("WaitForRule")
        assertThat(stringRep).contains("SELL")
        assertThat(stringRep).contains("7")
        assertThat(stringRep).contains("Hours")
    }

    @Test
    fun `should handle large time periods correctly`() {
        val lastTradeTime = Instant.parse("2023-01-01T12:00:00Z")
        val currentTime = Instant.parse("2024-01-01T12:00:00Z") // 365 days later

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 300,
            timeUnit = ChronoUnit.DAYS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue() // 365 days >= 300 days
    }

    @Test
    fun `should work with very short time periods - seconds`() {
        val lastTradeTime = Instant.parse("2024-01-10T12:00:00Z")
        val currentTime = Instant.parse("2024-01-10T12:00:45Z") // 45 seconds later

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeSELLTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.SELL,
            waitPeriod = 30,
            timeUnit = ChronoUnit.SECONDS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue() // 45 seconds >= 30 seconds
    }

    @Test
    fun `should handle millisecond precision correctly`() {
        val lastTradeTime = Instant.parse("2024-01-10T12:00:00.000Z")
        val currentTime = Instant.parse("2024-01-10T12:00:00.750Z") // 750ms later

        val mockContext = mockk<RuntimeContext>()
        every { mockContext.getValue(any<RuntimeValueResolver<Instant?>>()) } answers {
            val resolver = firstArg<RuntimeValueResolver<Instant>>()
            resolver.resolve(mockContext)
        }
        every { mockContext.getValue("currentTime") } returns currentTime
        every { mockContext.getValue("lastTradeBUYTime") } returns lastTradeTime

        val rule = WaitForRule(
            tradeType = TradeType.BUY,
            waitPeriod = 500,
            timeUnit = ChronoUnit.MILLIS,
            runtimeContext = mockContext
        )

        assertThat(rule.isSatisfied).isTrue() // 750ms >= 500ms
    }
}

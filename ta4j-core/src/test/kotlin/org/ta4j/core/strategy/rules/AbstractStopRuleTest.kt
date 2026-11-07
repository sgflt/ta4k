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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.analysis.cost.FixedTransactionCostModel
import org.ta4j.core.indicators.numeric.ConstantNumericIndicator
import org.ta4j.core.num.DecimalNumFactory
import org.ta4j.core.num.NaN
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.RuntimeContext

class AbstractStopRuleTest {

    private fun price(numFactory: NumFactory, value: Double) = ConstantNumericIndicator(numFactory.numOf(value))

    private fun fixedAmountStop(
        numFactory: NumFactory,
        currentPrice: Double,
        stopType: StopType,
        amount: Double,
        runtimeContext: RuntimeContext,
    ) = object : AbstractStopRule(
        price(numFactory, currentPrice),
        runtimeContext,
        stopType,
        StopDistance.fixedAmount(numFactory.numOf(amount))
    ) {}

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `stop price is null and rule unsatisfied without open position`(numFactory: NumFactory) {
        val rule = fixedAmountStop(numFactory, 0.0, StopType.LOSS, 3.0, PositionRuntimeContext(null))

        assertThat(rule.stopPrice).isNull()
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `stop price is placed on the correct side for each stop type and direction`(numFactory: NumFactory) {
        val long = PositionRuntimeContext.withOpenPosition(numFactory, 100.0, TradeType.BUY)
        val short = PositionRuntimeContext.withOpenPosition(numFactory, 100.0, TradeType.SELL)

        assertThat(fixedAmountStop(numFactory, 100.0, StopType.LOSS, 3.0, long).stopPrice)
            .isEqualTo(numFactory.numOf(97))
        assertThat(fixedAmountStop(numFactory, 100.0, StopType.GAIN, 3.0, long).stopPrice)
            .isEqualTo(numFactory.numOf(103))
        assertThat(fixedAmountStop(numFactory, 100.0, StopType.LOSS, 3.0, short).stopPrice)
            .isEqualTo(numFactory.numOf(103))
        assertThat(fixedAmountStop(numFactory, 100.0, StopType.GAIN, 3.0, short).stopPrice)
            .isEqualTo(numFactory.numOf(97))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fixed amount stop loss triggers at and beyond stop price`(numFactory: NumFactory) {
        val long = PositionRuntimeContext.withOpenPosition(numFactory, 100.0, TradeType.BUY)

        assertThat(fixedAmountStop(numFactory, 97.01, StopType.LOSS, 3.0, long).isSatisfied).isFalse()
        assertThat(fixedAmountStop(numFactory, 97.0, StopType.LOSS, 3.0, long).isSatisfied).isTrue()
        assertThat(fixedAmountStop(numFactory, 90.0, StopType.LOSS, 3.0, long).isSatisfied).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `stop is measured from net entry price including transaction cost`(numFactory: NumFactory) {
        val position = Position(
            startingType = TradeType.BUY,
            transactionCostModel = FixedTransactionCostModel(2.0),
            numFactory = numFactory
        ).apply { operate(Instant.EPOCH, numFactory.numOf(100), numFactory.one()) }
        val runtimeContext = PositionRuntimeContext(position)

        val rule = fixedAmountStop(numFactory, 99.5, StopType.LOSS, 3.0, runtimeContext)

        // net entry = 100 + 2 cost, stop = 102 - 3
        assertThat(rule.stopPrice).isEqualTo(numFactory.numOf(99))
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `closed position does not trigger`(numFactory: NumFactory) {
        val position = Position(startingType = TradeType.BUY, numFactory = numFactory).apply {
            operate(Instant.EPOCH, numFactory.numOf(100), numFactory.one())
            operate(Instant.EPOCH.plusSeconds(60), numFactory.numOf(90), numFactory.one())
        }

        val rule = fixedAmountStop(numFactory, 50.0, StopType.LOSS, 3.0, PositionRuntimeContext(position))

        assertThat(rule.stopPrice).isNull()
        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `unavailable current price does not trigger`(numFactory: NumFactory) {
        val runtimeContext = PositionRuntimeContext.withOpenPosition(numFactory, 100.0, TradeType.BUY)
        val rule = StopLossRule(ConstantNumericIndicator(NaN), numFactory.numOf(5), runtimeContext)

        assertThat(rule.isSatisfied).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun `fluent api creates stop rules on the price indicator`(numFactory: NumFactory) {
        val runtimeContext = PositionRuntimeContext.withOpenPosition(numFactory, 100.0, TradeType.BUY)

        val stopLoss = price(numFactory, 95.0).stopLossRule(5, runtimeContext)
        val takeProfit = price(numFactory, 110.0).takeProfitRule(10, runtimeContext)

        assertThat(stopLoss).isInstanceOf(StopLossRule::class.java)
        assertThat(stopLoss.isSatisfied).isTrue()
        assertThat(takeProfit).isInstanceOf(TakeProfitRule::class.java)
        assertThat(takeProfit.isSatisfied).isTrue()
    }

    @Test
    fun `stop distance rejects non positive values`() {
        val numFactory = DecimalNumFactory.getInstance()

        assertThatThrownBy { StopDistance.fixedAmount(numFactory.zero()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("must be positive")
        assertThatThrownBy { StopDistance.percentage(NaN) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}

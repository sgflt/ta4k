/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.criteria

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.analysis.Returns
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import kotlin.math.ceil
import kotlin.math.max

/**
 * Expected Shortfall criterion.
 *
 *
 * Calculates the mean of worst losses (below a certain percentile) for a trading strategy.
 * Also known as Conditional Value at Risk (CVaR).
 */
class ExpectedShortfallCriterion
/**
 * Constructor.
 *
 * @param numFactory the bar numFactory
 * @param confidenceLevel the confidence level (e.g. 0.95 for 95%)
 */(private val numFactory: NumFactory, private val confidenceLevel: Double) : AnalysisCriterion {
    override fun calculate(position: Position): Num {
        if (position.entry == null || position.exit == null) {
            log.debug("Position has no entry or exit")
            return this.numFactory.zero()
        }
        // TODO extract ReturnType to parameter
        val returns = Returns(this.numFactory, position, Returns.ReturnType.ARITHMETIC)
        val returnValues = returns.values.stream()
            .filter { num: Num? -> !num!!.isZero }  // Remove zero returns
            .toList()

        log.debug("Position returns (excluding zeros): {}", returnValues)
        return calculateExpectedShortfall(returnValues)
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            log.debug("Trading record is empty")
            return this.numFactory.zero()
        }

        val returns = Returns(this.numFactory, tradingRecord, Returns.ReturnType.ARITHMETIC)
        val returnValues = returns.values.stream()
            .filter { num: Num? -> !num!!.isZero }  // Remove zero returns
            .toList()

        log.debug("Trading record returns (excluding zeros): {}", returnValues)
        return calculateExpectedShortfall(returnValues)
    }


    override fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean {
        // Higher (less negative) expected shortfall is better
        return criterionValue1.isGreaterThan(criterionValue2)
    }


    private fun calculateExpectedShortfall(returns: MutableList<Num>): Num {
        log.debug("Calculating ES for returns: {}", returns)

        if (returns.isEmpty()) {
            log.debug("Returns list is empty")
            return this.numFactory.zero()
        }

        // Sort returns in ascending order (worst to best)
        val sortedReturns = returns.stream()
            .sorted()
            .toList()
        log.debug("Sorted returns: {}", sortedReturns)

        // Calculate number of returns to include (round up to ensure we take at least one)
        val numberOfReturns = max(1.0, ceil(sortedReturns.size * (1 - this.confidenceLevel)).toInt().toDouble()).toInt()
        log.debug(
            "Taking {} lowest returns (confidence level: {}, total returns: {})",
            numberOfReturns, this.confidenceLevel, sortedReturns.size
        )

        // Calculate average of values below threshold (worst returns)
        var sum = this.numFactory.zero()
        for (i in 0..<numberOfReturns) {
            sum = sum.plus(sortedReturns[i])
            log.debug(
                "Adding return at index {}: {}, running sum: {}",
                i,
                sortedReturns[i],
                sum
            )
        }

        val result = sum.dividedBy(this.numFactory.numOf(numberOfReturns))
        log.debug("Final ES result: {}", result)
        return result
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ExpectedShortfallCriterion::class.java)
    }
}

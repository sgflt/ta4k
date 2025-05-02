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

import lombok.extern.slf4j.Slf4j
import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory

/**
 * Return over maximum drawdown criterion (RoMaD).
 *
 * Measures strategy's risk-adjusted returns by dividing total return by maximum drawdown.
 * Higher values indicate better risk-adjusted performance.
 *
 * RoMaD = Total Return / Maximum Drawdown
 *
 * Interpretation:
 * - RoMaD > 1: Strategy generates more returns than its worst loss
 * - RoMaD < 1: Strategy's worst loss exceeds its returns
 * - RoMad = 0: No return
 * - RoMaD = NaN: no drawdown
 * - Negative RoMaD: Strategy lost money overall
 */
/**
 * Return over maximum drawdown criterion (RoMaD).
 */
@Slf4j
class ReturnOverMaxDrawdownCriterion(private val numFactory: NumFactory) : AnalysisCriterion {
    private val returnCriterion = ReturnCriterion(false)
    private val maxDrawdownCriterion = MaximumDrawdownCriterion(numFactory)


    override fun calculate(position: Position): Num {
        if (!position.isOpened) {
            return this.numFactory.zero()
        }

        val drawdown = this.maxDrawdownCriterion.calculate(position)
        val totalReturn = this.returnCriterion.calculate(position)

        log.debug(
            "Position entry: {}, exit: {}",
            position.entry!!.pricePerAsset,
            position.exit!!.pricePerAsset
        )
        log.debug("Is short: {}", position.entry!!.isSell)
        log.debug("Return: {}, Drawdown: {}", totalReturn, drawdown)

        val result = totalReturn.dividedBy(drawdown)
        log.debug("RoMaD result: {}", result)
        return result
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.isEmpty) {
            return this.numFactory.zero()
        }

        val drawdown = this.maxDrawdownCriterion.calculate(tradingRecord)
        val totalReturn = this.returnCriterion.calculate(tradingRecord)

        log.debug("Return: {}, Drawdown: {}", totalReturn, drawdown)

        val result = totalReturn.dividedBy(drawdown)
        log.debug("RoMaD result: {}", result)
        return result
    }


    override fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean {
        return criterionValue1.isGreaterThan(criterionValue2)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ReturnOverMaxDrawdownCriterion::class.java)
    }
}

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
package org.ta4j.core.backtest.criteria.pnl

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Return (in percentage) criterion (includes trading costs), returned in
 * decimal format.
 *
 *
 *
 * The return of the provided [position(s)][Position] over the provided
 * [series][BarSeries].
 */
class ReturnCriterion : AnalysisCriterion {
    /**
     * If true, then the base percentage of `1` (equivalent to 100%) is added
     * to the criterion value.
     */
    private val addBase: Boolean


    /**
     * Constructor with [.addBase] == true.
     */
    constructor() {
        this.addBase = true
    }


    /**
     * Constructor.
     *
     * @param addBase the [.addBase]
     */
    constructor(addBase: Boolean) {
        this.addBase = addBase
    }


    override fun calculate(position: Position): Num {
        return calculateProfit(position)!!
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        val product = tradingRecord.positions
            .map { calculateProfit(it) }
            .fold(defaultNumFactory.one()) { acc, multiplicand -> acc.multipliedBy(multiplicand!!) }

        return product.minus(if (addBase) defaultNumFactory.zero() else defaultNumFactory.one())
    }


    /** The higher the criterion value, the better.  */
    override fun betterThan(criterionValue1: Num, criterionValue2: Num): Boolean {
        return criterionValue1.isGreaterThan(criterionValue2)
    }


    /**
     * Calculates the gross return of a position (Buy and sell).
     *
     * @param position a position
     *
     * @return the gross return of the position
     */
    private fun calculateProfit(position: Position): Num? {
        if (position.isClosed) {
            return position.grossReturn
        }
        return if (this.addBase)
            defaultNumFactory.one()
        else
            defaultNumFactory.zero()
    }
}

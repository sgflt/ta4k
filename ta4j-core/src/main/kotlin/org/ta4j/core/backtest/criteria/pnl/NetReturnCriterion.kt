/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
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
import org.ta4j.core.num.Num

/**
 * Return (in percentage) criterion, returned in decimal format.
 *
 * This criterion uses the net return of the positions; trading costs are
 * deducted from the calculation. It represents the percentage change including
 * the base value.
 *
 * The return of the provided [Position] position(s).
 */
class NetReturnCriterion(addBase: Boolean = true) : AbstractReturnCriterion(addBase) {

    override fun calculateReturn(position: Position): Num {
        val entry = position.entry!!
        val amount = entry.amount
        val netPrice = entry.netPrice
        val entryValue = netPrice * amount
        val one = position.numFactory.one()
        
        if (entryValue.isZero) {
            return one
        }
        
        val profit = position.profit
        return profit / entryValue + one
    }
}
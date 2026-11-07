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
package org.ta4j.core.strategy.rules

import org.ta4j.core.num.Num

/**
 * Distance between a reference price (entry or trailing extreme) and the stop price.
 *
 * Stop rules combine a [StopDistance] with a [StopType], so new kinds of stops
 * (percentage, fixed amount, ATR multiple, ...) need no new rule subclass.
 */
fun interface StopDistance {

    /**
     * @param referencePrice price the stop is measured from
     *
     * @return non-negative distance in price units
     */
    fun distanceFrom(referencePrice: Num): Num

    companion object {
        /**
         * @param percentage distance as a percentage of the reference price (e.g. 5 for 5 %)
         */
        fun percentage(percentage: Num): StopDistance {
            require(!percentage.isNaN && percentage.isPositive) {
                "Stop percentage must be positive, but was: $percentage"
            }
            return StopDistance { referencePrice ->
                referencePrice * percentage / referencePrice.numFactory.hundred()
            }
        }

        /**
         * @param amount distance in absolute price units
         */
        fun fixedAmount(amount: Num): StopDistance {
            require(!amount.isNaN && amount.isPositive) {
                "Stop amount must be positive, but was: $amount"
            }
            return StopDistance { amount }
        }
    }
}

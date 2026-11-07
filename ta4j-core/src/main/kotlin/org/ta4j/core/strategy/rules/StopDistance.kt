/*
 * SPDX-License-Identifier: MIT
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

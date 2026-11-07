/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
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
         * @param volatility indicator with the current volatility in price units (e.g. ATR);
         * it must be registered in the indicator context so it updates on every bar.
         * While it is not stable the distance is NaN, so the stop stays inactive.
         * @param coefficient multiple of [volatility] used as distance
         */
        fun volatility(volatility: NumericIndicator, coefficient: Num): StopDistance {
            require(!coefficient.isNaN && coefficient.isPositive) {
                "Volatility coefficient must be positive, but was: $coefficient"
            }
            return StopDistance { if (volatility.isStable) volatility.value * coefficient else NaN }
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

/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.execution

import org.ta4j.core.TradeType
import org.ta4j.core.num.Num

/**
 * Adjusts an execution price against the trader: buys fill higher, sells fill lower.
 */
fun interface SlippageModel {
    /**
     * @param side whether the fill buys or sells
     * @param price the price before slippage
     * @return the filled price
     */
    fun apply(side: TradeType, price: Num): Num

    companion object {
        /** Fills at the requested price. */
        val NONE = SlippageModel { _, price -> price }

        /** Fills [amount] per unit worse than the requested price. */
        fun fixed(amount: Number) = SlippageModel { side, price ->
            val slippage = price.numFactory.numOf(amount)
            if (side == TradeType.BUY) price + slippage else price - slippage
        }

        /** Fills [percentage] % worse than the requested price, e.g. 0.1 for 0.1 %. */
        fun percentage(percentage: Number): SlippageModel {
            require(percentage.toDouble() >= 0.0) { "Slippage percentage must not be negative" }
            return SlippageModel { side, price ->
                val ratio = price.numFactory.numOf(percentage) / price.numFactory.hundred()
                val one = price.numFactory.one()
                if (side == TradeType.BUY) price * (one + ratio) else price * (one - ratio)
            }
        }
    }
}

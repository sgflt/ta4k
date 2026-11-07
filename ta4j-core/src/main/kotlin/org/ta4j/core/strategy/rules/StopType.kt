/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.TradeType
import org.ta4j.core.num.Num

/**
 * Which side of the position a stop protects.
 */
enum class StopType {
    /** Exits when price moves against the position (stop-loss). */
    LOSS {
        override fun stopPrice(tradeType: TradeType, referencePrice: Num, distance: Num) =
            when (tradeType) {
                TradeType.BUY -> referencePrice - distance
                TradeType.SELL -> referencePrice + distance
            }

        override fun isTriggered(tradeType: TradeType, stopPrice: Num, currentPrice: Num) =
            when (tradeType) {
                TradeType.BUY -> currentPrice <= stopPrice
                TradeType.SELL -> currentPrice >= stopPrice
            }
    },

    /** Exits when price moves in favour of the position (take-profit / stop-gain). */
    GAIN {
        override fun stopPrice(tradeType: TradeType, referencePrice: Num, distance: Num) =
            LOSS.stopPrice(tradeType.complementType(), referencePrice, distance)

        override fun isTriggered(tradeType: TradeType, stopPrice: Num, currentPrice: Num) =
            LOSS.isTriggered(tradeType.complementType(), stopPrice, currentPrice)
    };

    /**
     * @return price at which the stop fires for a position entered with [tradeType]
     */
    abstract fun stopPrice(tradeType: TradeType, referencePrice: Num, distance: Num): Num

    /**
     * @return true if [currentPrice] reached [stopPrice] for a position entered with [tradeType]
     */
    abstract fun isTriggered(tradeType: TradeType, stopPrice: Num, currentPrice: Num): Boolean
}

/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

/** The type of a trade  */
enum class TradeType {
    /** A BUY corresponds to a *BID* trade.  */
    BUY {
        override fun complementType(): TradeType {
            return SELL
        }
    },

    /** A SELL corresponds to an *ASK* trade.  */
    SELL {
        override fun complementType(): TradeType {
            return BUY
        }
    };


    /**
     * @return the complementary trade type
     */
    abstract fun complementType(): TradeType
}

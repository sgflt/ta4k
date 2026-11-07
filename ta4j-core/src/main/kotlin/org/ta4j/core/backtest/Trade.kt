/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest

import java.time.Instant
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.analysis.cost.CostModel
import org.ta4j.core.num.Num

/**
 * A `Trade` is defined by:
 *
 *
 *  * the index (in the [bar series][BarSeries]) on which the trade is
 * executed
 *  * a [type][TradeType] (BUY or SELL)
 *  * a pricePerAsset (optional)
 *  * a trade amount (optional)
 *
 *
 * A [position][Position] is a pair of complementary trades.
 */
class Trade(
    /** The index the trade was executed.  */
    val whenExecuted: Instant,
    /** The trade price per asset.  */
    val pricePerAsset: Num,
    /** The type of the trade.  */
    val type: TradeType,
    /** The type of the trade.  */
    val orderType: OrderType,
    /** The trade amount.  */
    val amount: Num,
    /** The cost model for trade execution.  */
    private var transactionCostModel: CostModel,
) {
    enum class OrderType {
        OPEN,
        CLOSE,
    }

    /**
     * The net price per asset for the trade (i.e. [.pricePerAsset] with
     * [.cost]).
     */
    lateinit var netPrice: Num

    /** The cost for executing the trade.  */
    lateinit var cost: Num


    /**
     * Constructor.
     *
     * @param whenExecuted the time of execution
     * @param type the trade type
     * @param pricePerAsset the trade price per asset
     * @param amount the trade amount
     * @param transactionCostModel the cost model for trade execution
     */
    init {
        setPricesAndCost(amount, transactionCostModel)
    }


    /**
     * Sets the raw and net prices of the trade.
     *
     * @param pricePerAsset the raw price of the asset
     * @param amount the amount of assets ordered
     * @param transactionCostModel the cost model for trade execution
     */
    private fun setPricesAndCost(amount: Num, transactionCostModel: CostModel) {
        cost = transactionCostModel.calculate(pricePerAsset, amount)

        val costPerAsset = cost / amount
        // onCandle transaction costs to the pricePerAsset at the trade
        netPrice = if (isBuy) {
            pricePerAsset.plus(costPerAsset)
        } else {
            pricePerAsset.minus(costPerAsset)
        }
    }


    val isBuy: Boolean
        /**
         * @return true if this is a BUY trade, false otherwise
         */
        get() = type === TradeType.BUY


    val isSell: Boolean
        /**
         * @return true if this is a SELL trade, false otherwise
         */
        get() = type === TradeType.SELL


    val value: Num
        /**
         * @return the market value of a trade (without transaction cost)
         */
        get() = pricePerAsset * amount


    override fun toString(): String {
        return "$type $orderType $whenExecuted | $amount @ $pricePerAsset"
    }
}

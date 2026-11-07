/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.rules

import org.ta4j.core.TradeType
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/**
 * Satisfied when a planned trade offers at least [minRiskReward] reward per unit of risk.
 *
 * For BUY setups: risk = price - stop, reward = target - price; requires stop < price < target.
 * For SELL setups: risk = stop - price, reward = price - target; requires target < price < stop.
 *
 * Not satisfied when any input is NaN or the stop/target are on the wrong side of the price.
 *
 * @param priceIndicator the planned entry price
 * @param stopIndicator the planned stop price
 * @param targetIndicator the planned target price
 * @param tradeType direction of the planned trade
 * @param minRiskReward minimum accepted reward / risk ratio
 */
class RiskRewardRatioRule(
    private val priceIndicator: NumericIndicator,
    private val stopIndicator: NumericIndicator,
    private val targetIndicator: NumericIndicator,
    private val tradeType: TradeType,
    val minRiskReward: Num,
) : AbstractRule() {

    constructor(
        priceIndicator: NumericIndicator,
        stopIndicator: NumericIndicator,
        targetIndicator: NumericIndicator,
        tradeType: TradeType,
        minRiskReward: Number,
    ) : this(
        priceIndicator,
        stopIndicator,
        targetIndicator,
        tradeType,
        priceIndicator.numFactory.numOf(minRiskReward)
    )

    init {
        require(!minRiskReward.isNaN && minRiskReward.isPositive) {
            "minRiskReward must be positive, but was: $minRiskReward"
        }
    }

    /**
     * Current reward / risk ratio, or null when the setup is invalid.
     */
    val riskReward: Num?
        get() {
            val price = priceIndicator.value
            val stop = stopIndicator.value
            val target = targetIndicator.value
            if (price.isNaN || stop.isNaN || target.isNaN) {
                return null
            }

            val (risk, reward) = when (tradeType) {
                TradeType.BUY -> (price - stop) to (target - price)
                TradeType.SELL -> (stop - price) to (price - target)
            }
            if (!risk.isPositive || !reward.isPositive) {
                return null
            }
            return reward / risk
        }

    override val isSatisfied: Boolean
        get() {
            val satisfied = riskReward?.let { it >= minRiskReward } ?: false
            traceIsSatisfied(satisfied)
            return satisfied
        }

    override fun toString() = "RiskRewardRatioRule[$tradeType >= $minRiskReward] => $isSatisfied"
}

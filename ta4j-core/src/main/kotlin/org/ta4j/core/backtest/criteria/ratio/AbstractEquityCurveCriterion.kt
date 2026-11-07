/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.analysis.EquityPoint
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider

/**
 * Criterion computed from an equity curve.
 *
 * A trading record supplies [TradingRecord.equityCurve] (all bars, flat periods included). A single
 * position supplies its own bars from entry on, so flat periods do not exist there.
 */
abstract class AbstractEquityCurveCriterion : AnalysisCriterion {

    override fun calculate(position: Position): Num = calculate(curveOf(position), numFactoryOf(position))

    override fun calculate(tradingRecord: TradingRecord): Num {
        val numFactory = tradingRecord.positions.firstOrNull()?.let(::numFactoryOf)
            ?: tradingRecord.currentPosition.entry?.pricePerAsset?.numFactory
            ?: tradingRecord.equityCurve.firstOrNull()?.equity?.numFactory
            ?: NumFactoryProvider.defaultNumFactory
        return calculate(tradingRecord.equityCurve, numFactory)
    }

    /**
     * @param curve equity points in time order
     */
    protected abstract fun calculate(curve: List<EquityPoint>, numFactory: NumFactory): Num

    private fun numFactoryOf(position: Position): NumFactory =
        position.entry?.pricePerAsset?.numFactory ?: NumFactoryProvider.defaultNumFactory

    private fun curveOf(position: Position): List<EquityPoint> {
        val entry = position.entry ?: return emptyList()
        val start = EquityPoint(entry.whenExecuted, entry.pricePerAsset.numFactory.one(), true)
        return listOf(start) + position.cashFlow
            .filter { it.`when`.isAfter(entry.whenExecuted) }
            .map { EquityPoint(it.`when`, it.value, true) }
    }
}

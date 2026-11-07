/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.TradeType
import org.ta4j.core.backtest.analysis.CashFlow
import org.ta4j.core.backtest.criteria.AnalysisCriterion.PositionFilter
import org.ta4j.core.backtest.criteria.pnl.AverageLossCriterion
import org.ta4j.core.backtest.criteria.pnl.AverageProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.LossCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossPercentageCriterion
import org.ta4j.core.backtest.criteria.pnl.ProfitLossRatioCriterion
import org.ta4j.core.backtest.criteria.pnl.ReturnCriterion
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.DecimalNumFactory
import org.ta4j.core.num.NumFactoryProvider

/**
 * Criteria must compute with the number factory of the record they measure, not the global default.
 */
internal class CriteriaNumFactoryTest {

    private val decimal = DecimalNumFactory.getInstance()

    private fun decimalRecord() = BackTestTradingRecord(startingType = TradeType.BUY, numFactory = decimal).apply {
        val start = Instant.EPOCH
        enter(start, decimal.numOf(100), decimal.one())
        exit(start.plus(Duration.ofMinutes(5)), decimal.numOf(110), decimal.one())
        enter(start.plus(Duration.ofMinutes(10)), decimal.numOf(110), decimal.one())
        exit(start.plus(Duration.ofMinutes(20)), decimal.numOf(99), decimal.one())
    }

    private val criteria: List<AnalysisCriterion> = listOf(
        NumberOfPositionsCriterion(),
        NumberOfWinningPositionsCriterion(),
        NumberOfLosingPositionsCriterion(),
        NumberOfBreakEvenPositionsCriterion(),
        NumberOfConsecutivePositionsCriterion(PositionFilter.PROFIT),
        PositionsRatioCriterion(PositionFilter.LOSS),
        TimeInTradeCriterion.minutes(),
        ExpectancyCriterion(),
        SqnCriterion(),
        LinearTransactionCostCriterion(1000.0, 0.005),
        ProfitLossRatioCriterion(),
        AverageProfitCriterion(),
        AverageLossCriterion(),
        ProfitLossPercentageCriterion(),
        ReturnCriterion(),
        ProfitCriterion(false),
        LossCriterion(false),
        ProfitLossCriterion(),
    )

    @Test
    fun criteriaUseTheNumFactoryOfTheRecord() {
        assertThat(NumFactoryProvider.defaultNumFactory).isNotSameAs(decimal)
        val record = decimalRecord()
        for (criterion in criteria) {
            assertThat(criterion.calculate(record))
                .`as`(criterion::class.simpleName)
                .isInstanceOf(DecimalNum::class.java)
            assertThat(criterion.calculate(record.positions.first()))
                .`as`("${criterion::class.simpleName} for a position")
                .isInstanceOf(DecimalNum::class.java)
        }
    }

    @Test
    fun cashFlowDefaultsToTheNumFactoryOfTheRecord() {
        val cashFlow = CashFlow(decimalRecord())
        assertThat(cashFlow.getValue(Instant.MAX)).isInstanceOf(DecimalNum::class.java)
    }
}

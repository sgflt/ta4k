package org.ta4j.core.criteria

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.backtest.criteria.LinearTransactionCostCriterion
import org.ta4j.core.num.NumFactory

class LinearTransactionCostCriterionTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun externalData(numFactory: NumFactory) {
        val xls = XLSCriterionTest(this.javaClass, "LTC.xls", 16, 6, numFactory)
        val tradingRecord = xls.tradingRecord

        // Skip test if XLS file doesn't contain trading data  
        if (tradingRecord.positions.isEmpty()) {
            org.junit.jupiter.api.Assumptions.assumeFalse(
                tradingRecord.positions.isEmpty(),
                "Skipping external data test - XLS file contains no trading positions"
            )
        }

        // Test with first set of parameters
        val criterion1 = LinearTransactionCostCriterion(1000.0, 0.005, 0.2)
        val result1 = criterion1.calculate(tradingRecord)
        org.ta4j.core.TestUtils.assertNumEquals(843.5492, result1)

        // Test with second set of parameters
        val criterion2 = LinearTransactionCostCriterion(1000.0, 0.1, 1.0)
        val result2 = criterion2.calculate(tradingRecord)
        org.ta4j.core.TestUtils.assertNumEquals(1122.4410, result2)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun dummyData(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 150.0, 200.0, 100.0, 50.0, 100.0)
            .toTradingRecordContext()
            .withCriterion(LinearTransactionCostCriterion(1000.0, 0.005, 0.2))

        context.enter(1.0).asap()
            .exit(1.0).asap()
            .assertResults(12.861)

        context.enter(1.0).asap()
            .exit(1.0).asap()
            .assertResults(24.3759)

        context.enter(1.0).asap()
            .assertResults(28.2488)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun fixedCost(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .withCandlePrices(100.0, 105.0, 110.0, 100.0, 95.0, 105.0)
            .toTradingRecordContext()
            .withCriterion(LinearTransactionCostCriterion(1000.0, 0.0, 1.3))

        context.enter(1.0).asap()
            .exit(1.0).asap()
            .assertResults(2.6)

        context.enter(1.0).asap()
            .exit(1.0).asap()
            .assertResults(5.2)

        context.enter(1.0).asap()
            .assertResults(6.5)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun fixedCostWithOnePosition(numFactory: NumFactory) {
        val context = MarketEventTestContext()
            .withNumFactory(numFactory)
            .toTradingRecordContext()
            .withCriterion(LinearTransactionCostCriterion(1000.0, 0.0, 0.75))

        context.assertResults(0.0)

        context.enter(1.0).at(2.0)
            .assertResults(0.75)

        context.exit(1.0).at(4.0)
            .assertResults(1.5)

        context.enter(1.0).at(5.0)
            .assertResults(2.25)
    }
}

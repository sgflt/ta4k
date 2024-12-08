package org.ta4j.core.criteria;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.num.NumFactory;

class LinearTransactionCostCriterionTest {

// TODO how to load trading record from events?
//  @ParameterizedTest
//  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
//  void externalData(final NumFactory numFactory) throws Exception {
//    var xls = new XLSCriterionTest(this.getClass(), "LTC.xls", 16, 6, numFactory);
//    final var context = new MarketEventTestContext()
//        .withMarketEvents(xls.getMarketEvents())
//        .withNumFactory(numFactory)
//        .toTradingRecordContext()
//        .withTradingRecord(xls.getTradingRecord());
//
//    context
//        .withCriterion(new LinearTransactionCostCriterion(1000d, 0.005, 0.2))
//        .assertResults(843.5492);
//
//    context
//        .withCriterion(new LinearTransactionCostCriterion(1000d, 0.1, 1.0))
//        .assertResults(1122.4410);
//  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void dummyData(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 150, 200, 100, 50, 100)
        .toTradingRecordContext()
        .withCriterion(new LinearTransactionCostCriterion(1000d, 0.005, 0.2));

    context.enter(1).asap()
        .exit(1).asap()
        .assertResults(12.861);

    context.enter(1).asap()
        .exit(1).asap()
        .assertResults(24.3759);

    context.enter(1).asap()
        .assertResults(28.2488);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void fixedCost(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .withCandlePrices(100, 105, 110, 100, 95, 105)
        .toTradingRecordContext()
        .withCriterion(new LinearTransactionCostCriterion(1000d, 0d, 1.3d));

    context.enter(1).asap()
        .exit(1).asap()
        .assertResults(2.6d);

    context.enter(1).asap()
        .exit(1).asap()
        .assertResults(5.2d);

    context.enter(1).asap()
        .assertResults(6.5d);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void fixedCostWithOnePosition(final NumFactory numFactory) {
    final var context = new MarketEventTestContext()
        .withNumFactory(numFactory)
        .toTradingRecordContext()
        .withCriterion(new LinearTransactionCostCriterion(1000d, 0d, 0.75d));

    context.assertResults(0d);

    context.enter(1).at(2)
        .assertResults(0.75d);

    context.exit(1).at(4)
        .assertResults(1.5d);

    context.enter(1).at(5)
        .assertResults(2.25d);
  }
}

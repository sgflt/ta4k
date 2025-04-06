package org.ta4j.core.indicators.numeric.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.MarketEventTestContext;
import org.ta4j.core.api.Indicators;
import org.ta4j.core.num.NumFactory;


class StandardDeviationIndicatorTest {
  private MarketEventTestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new MarketEventTestContext();
    this.testContext.withCandlePrices(1, 2, 3, 4, 3, 4, 5, 4, 3, 0, 9);
  }


  @ParameterizedTest(name = "STDEV [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void standardDeviationUsingBarCount4UsingClosePrice(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var sdv = Indicators.closePrice().stddev(4);

    this.testContext.withIndicator(sdv)
        .fastForwardUntilStable()
        .assertCurrent(1.291)
        .assertNext(0.81649)
        .assertNext(0.57735)
        .assertNext(0.81649)
        .assertNext(0.81649)
        .assertNext(0.81649)
        .assertNext(2.1602)
        .assertNext(3.7416);
  }
}

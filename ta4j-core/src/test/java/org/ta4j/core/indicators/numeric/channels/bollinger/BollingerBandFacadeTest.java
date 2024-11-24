/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators.numeric.channels.bollinger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.TestContext;
import org.ta4j.core.indicators.numeric.Indicators;
import org.ta4j.core.indicators.numeric.average.SMAIndicator;
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.NumFactory;

class BollingerBandFacadeTest {


  private TestContext testContext;


  @BeforeEach
  void setUp() {
    this.testContext = new TestContext();
    this.testContext.withCandlePrices(
        1, 2, 3, 4, 3, 4, 5, 4, 3, 3, 4, 3, 2
    );
  }


  @ParameterizedTest(name = "PCB [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void pcbFromFacadeIsCorrect(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePriceIndicator = Indicators.closePrice();

    final var pcb = new PercentBIndicator(closePriceIndicator, 5, 2);
    final var pcbNumeric = new BollingerBandFacade(5, 2).percentB();

    this.testContext.withIndicators(pcb, pcbNumeric)
        .assertIndicatorEquals(pcb, pcbNumeric);
  }


  @ParameterizedTest(name = "Middle BB [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void middleBBFromFacadeIsCorrect(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePriceIndicator = Indicators.closePrice();
    final int barCount = 3;
    final var sma = new SMAIndicator(closePriceIndicator, 3);

    final var middleBB = new BollingerBandsMiddleIndicator(sma);
    final var bollingerBandFacade = new BollingerBandFacade(barCount, 2);
    final var middleBBNumeric = bollingerBandFacade.middle();

    this.testContext.withIndicators(middleBB, middleBBNumeric)
        .assertIndicatorEquals(middleBB, middleBBNumeric);
  }


  @ParameterizedTest(name = "Lower BB [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void lowerBBFromFacadeIsCorrect(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePriceIndicator = Indicators.closePrice();
    final int barCount = 3;
    final var sma = new SMAIndicator(closePriceIndicator, 3);

    final var middleBB = new BollingerBandsMiddleIndicator(sma);
    final var standardDeviation = new StandardDeviationIndicator(closePriceIndicator, barCount);
    final var lowerBB = new BollingerBandsLowerIndicator(middleBB, standardDeviation);

    final var bollingerBandFacade = new BollingerBandFacade(barCount, 2);
    final var lowerBBNumeric = bollingerBandFacade.lower();

    this.testContext.withIndicators(lowerBB, lowerBBNumeric)
        .assertIndicatorEquals(lowerBB, lowerBBNumeric);
  }


  @ParameterizedTest(name = "Upper BB [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void upperBBFromFacadeIsCorrect(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePriceIndicator = Indicators.closePrice();
    final int barCount = 3;
    final var sma = new SMAIndicator(closePriceIndicator, 3);

    final var middleBB = new BollingerBandsMiddleIndicator(sma);
    final var standardDeviation = new StandardDeviationIndicator(closePriceIndicator, barCount);
    final var upperBB = new BollingerBandsUpperIndicator(middleBB, standardDeviation);

    final var bollingerBandFacade = new BollingerBandFacade(barCount, 2);
    final var upperBBNumeric = bollingerBandFacade.upper();

    this.testContext.withIndicators(upperBB, upperBBNumeric)
        .assertIndicatorEquals(upperBB, upperBBNumeric);
  }


  @ParameterizedTest(name = "Width of BB [{index}] {0}")
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void widthBBFromFacadeIsCorrect(final NumFactory numFactory) {
    this.testContext.withNumFactory(numFactory);

    final var closePriceIndicator = Indicators.closePrice();
    final int barCount = 3;
    final var sma = new SMAIndicator(closePriceIndicator, 3);

    final var middleBB = new BollingerBandsMiddleIndicator(sma);
    final var standardDeviation = new StandardDeviationIndicator(closePriceIndicator, barCount);
    final var lowerBB = new BollingerBandsLowerIndicator(middleBB, standardDeviation);
    final var upperBB = new BollingerBandsUpperIndicator(middleBB, standardDeviation);
    final var widthBB = new BollingerBandWidthIndicator(upperBB, middleBB, lowerBB);

    final var bollingerBandFacade = new BollingerBandFacade(barCount, 2);
    final var widthBBNumeric = bollingerBandFacade.bandwidth();

    this.testContext.withIndicators(widthBB, widthBBNumeric)
        .assertIndicatorEquals(widthBB, widthBBNumeric);
  }
}

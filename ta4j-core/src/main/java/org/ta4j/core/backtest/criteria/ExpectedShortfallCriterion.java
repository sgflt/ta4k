/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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
package org.ta4j.core.backtest.criteria;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.backtest.BacktestBarSeries;
import org.ta4j.core.backtest.Position;
import org.ta4j.core.backtest.TradingRecord;
import org.ta4j.core.backtest.analysis.Returns;
import org.ta4j.core.num.Num;

/**
 * Expected Shortfall criterion.
 * <p>
 * Calculates the mean of worst losses (below a certain percentile) for a trading strategy.
 * Also known as Conditional Value at Risk (CVaR).
 */
public class ExpectedShortfallCriterion implements AnalysisCriterion {

  private static final Logger log = LoggerFactory.getLogger(ExpectedShortfallCriterion.class);
  private final BacktestBarSeries series;
  private final double confidenceLevel;


  /**
   * Constructor.
   *
   * @param series the bar series
   * @param confidenceLevel the confidence level (e.g. 0.95 for 95%)
   */
  public ExpectedShortfallCriterion(final BacktestBarSeries series, final double confidenceLevel) {
    this.series = series;
    this.confidenceLevel = confidenceLevel;
  }


  @Override
  public Num calculate(final Position position) {
    if (position.getEntry() == null || position.getExit() == null) {
      log.debug("Position has no entry or exit");
      return this.series.numFactory().zero();
    }
    // TODO extract ReturnType to parameter
    final var returns = new Returns(this.series.numFactory(), position, Returns.ReturnType.ARITHMETIC);
    final var returnValues = returns.getValues().stream()
        .filter(num -> !num.isZero()) // Remove zero returns
        .toList();

    log.debug("Position returns (excluding zeros): {}", returnValues);
    return calculateExpectedShortfall(returnValues);
  }


  @Override
  public Num calculate(final TradingRecord tradingRecord) {
    if (tradingRecord.getPositions().isEmpty()) {
      log.debug("Trading record is empty");
      return this.series.numFactory().zero();
    }

    final var returns = new Returns(this.series.numFactory(), tradingRecord, Returns.ReturnType.ARITHMETIC);
    final var returnValues = returns.getValues().stream()
        .filter(num -> !num.isZero()) // Remove zero returns
        .toList();

    log.debug("Trading record returns (excluding zeros): {}", returnValues);
    return calculateExpectedShortfall(returnValues);
  }


  @Override
  public boolean betterThan(final Num criterionValue1, final Num criterionValue2) {
    // Higher (less negative) expected shortfall is better
    return criterionValue1.isGreaterThan(criterionValue2);
  }


  private Num calculateExpectedShortfall(final List<Num> returns) {
    log.debug("Calculating ES for returns: {}", returns);

    if (returns.isEmpty()) {
      log.debug("Returns list is empty");
      return this.series.numFactory().zero();
    }

    // Sort returns in ascending order (worst to best)
    final var sortedReturns = returns.stream()
        .sorted()
        .toList();
    log.debug("Sorted returns: {}", sortedReturns);

    // Calculate number of returns to include (round up to ensure we take at least one)
    final var numberOfReturns = Math.max(1, (int) Math.ceil(sortedReturns.size() * (1 - this.confidenceLevel)));
    log.debug(
        "Taking {} lowest returns (confidence level: {}, total returns: {})",
        numberOfReturns, this.confidenceLevel, sortedReturns.size()
    );

    // Calculate average of values below threshold (worst returns)
    var sum = this.series.numFactory().zero();
    for (int i = 0; i < numberOfReturns; i++) {
      sum = sum.plus(sortedReturns.get(i));
      log.debug("Adding return at index {}: {}, running sum: {}", i, sortedReturns.get(i), sum);
    }

    final var result = sum.dividedBy(this.series.numFactory().numOf(numberOfReturns));
    log.debug("Final ES result: {}", result);
    return result;
  }
}

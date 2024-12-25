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
package org.ta4j.core.indicators.numeric;

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.api.Indicator;
import org.ta4j.core.api.series.Bar;
import org.ta4j.core.indicators.bool.helpers.CrossIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator;
import org.ta4j.core.indicators.numeric.average.DoubleEMAIndicator;
import org.ta4j.core.indicators.numeric.average.EMAIndicator;
import org.ta4j.core.indicators.numeric.average.HMAIndicator;
import org.ta4j.core.indicators.numeric.average.KAMAIndicator;
import org.ta4j.core.indicators.numeric.average.LWMAIndicator;
import org.ta4j.core.indicators.numeric.average.MMAIndicator;
import org.ta4j.core.indicators.numeric.average.SMAIndicator;
import org.ta4j.core.indicators.numeric.average.TripleEMAIndicator;
import org.ta4j.core.indicators.numeric.average.WMAIndicator;
import org.ta4j.core.indicators.numeric.average.ZLEMAIndicator;
import org.ta4j.core.indicators.numeric.helpers.GainIndicator;
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.numeric.helpers.LossIndicator;
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.numeric.helpers.RunningTotalIndicator;
import org.ta4j.core.indicators.numeric.momentum.RSIIndicator;
import org.ta4j.core.indicators.numeric.operation.BinaryOperation;
import org.ta4j.core.indicators.numeric.operation.UnaryOperation;
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonUpIndicator;
import org.ta4j.core.indicators.numeric.statistics.CovarianceIndicator;
import org.ta4j.core.indicators.numeric.statistics.MeanDeviationIndicator;
import org.ta4j.core.indicators.numeric.statistics.SigmaIndicator;
import org.ta4j.core.indicators.numeric.statistics.SimpleLinearRegressionIndicator;
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.numeric.statistics.StandardErrorIndicator;
import org.ta4j.core.indicators.numeric.statistics.VarianceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * NumericIndicator is a fluent class. It provides
 * methods to create rules and other "lightweight" indicators, using a
 * (hopefully) natural-looking and expressive series of method calls.
 *
 * <p>
 * Methods like plus(), minus() and sqrt() correspond directly to methods in the
 * {@code Num} interface. These methods create "lightweight" (not cached)
 * indicators to add, subtract, etc. Many methods are overloaded to accept
 * either {@code NumericIndicator} or {@code Number} arguments.
 *
 * <p>
 * Methods like sma() and ema() simply create the corresponding indicator
 * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first
 * argument. These methods usually instantiate cached objects.
 *
 * <p>
 * Another set of methods, like crossedOver() and isGreaterThan() create Rule
 * objects. These are also overloaded to accept both {@code NumericIndicator} and
 * {@code Number} arguments.
 */
@Slf4j
public abstract class NumericIndicator implements Indicator<Num> {

  /**
   * Factory used for creation of this indicator that will be propagated to child indicators.
   */
  private final NumFactory numFactory;
  private Instant currentBeginTime = Instant.MIN;
  protected Num value;


  protected NumericIndicator(final NumFactory numFactory) {
    this.numFactory = numFactory;
  }


  public AroonUpIndicator aroonUp(final int barCount) {
    return new AroonUpIndicator(this.numFactory, this, barCount);
  }


  public AroonDownIndicator aroonDown(final int barCount) {
    return new AroonDownIndicator(this.numFactory, this, barCount);
  }


  public DifferenceIndicator difference() {
    return new DifferenceIndicator(this);
  }


  public GainIndicator gain() {
    return new GainIndicator(this);
  }


  public LossIndicator loss() {
    return new LossIndicator(this);
  }


  public RSIIndicator rsi(final int barCount) {
    return new RSIIndicator(this, barCount);
  }


  public NumFactory getNumFactory() {
    return this.numFactory;
  }


  /**
   * @param other the other indicator
   *
   * @return {@code this + other}, rounded as necessary
   */
  public BinaryOperation plus(final NumericIndicator other) {
    return BinaryOperation.sum(this, other);
  }


  /**
   * @param n the other number
   *
   * @return {@code this + n}, rounded as necessary
   */
  public BinaryOperation plus(final Number n) {
    return plus(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return {@code this - other}, rounded as necessary
   */
  public BinaryOperation minus(final NumericIndicator other) {
    return BinaryOperation.difference(this, other);
  }


  /**
   * @param n the other number
   *
   * @return {@code this - n}, rounded as necessary
   */
  public BinaryOperation minus(final Number n) {
    return minus(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return {@code this * other}, rounded as necessary
   */
  public BinaryOperation multipliedBy(final NumericIndicator other) {
    return BinaryOperation.product(this, other);
  }


  /**
   * @param n the other number
   *
   * @return {@code this * n}, rounded as necessary
   */
  public NumericIndicator multipliedBy(final Number n) {
    return multipliedBy(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return {@code this / other}, rounded as necessary
   */
  public BinaryOperation dividedBy(final NumericIndicator other) {
    return BinaryOperation.quotient(this, other);
  }


  /**
   * @param n the other number
   *
   * @return {@code this / n}, rounded as necessary
   */
  public NumericIndicator dividedBy(final Number n) {
    return dividedBy(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return the smaller of {@code this} and {@code other}; if they are equal,
   *     {@code this} is returned.
   */
  public BinaryOperation min(final NumericIndicator other) {
    return BinaryOperation.min(this, other);
  }


  /**
   * @param n the other number
   *
   * @return the smaller of {@code this} and {@code n}; if they are equal,
   *     {@code this} is returned.
   */
  public BinaryOperation min(final Number n) {
    return min(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return the greater of {@code this} and {@code other}; if they are equal,
   *     {@code this} is returned.
   */
  public BinaryOperation max(final NumericIndicator other) {
    return BinaryOperation.max(this, other);
  }


  /**
   * @param n the other number
   *
   * @return the greater of {@code this} and {@code n}; if they are equal,
   *     {@code this} is returned.
   */
  public BinaryOperation max(final Number n) {
    return max(createConstant(n));
  }


  /**
   * Returns an Indicator whose values are the absolute values of {@code this}.
   *
   * @return {@code abs(this)}
   */
  public UnaryOperation abs() {
    return UnaryOperation.abs(this);
  }


  /**
   * Returns an Indicator whose values are √(this).
   *
   * @return {@code √(this)}
   */
  public UnaryOperation sqrt() {
    return UnaryOperation.sqrt(this);
  }


  /**
   * Returns an Indicator whose values are {@code this * this}.
   *
   * @return {@code this * this}
   */
  public NumericIndicator squared() {
    // TODO: implement pow(n); a few others
    return this.multipliedBy(this);
  }


  /**
   * @param barCount the time frame
   *
   * @return the {@link SMAIndicator} of {@code this}
   */
  public SMAIndicator sma(final int barCount) {
    return new SMAIndicator(this, barCount);
  }


  /**
   * @param barCount the time frame
   *
   * @return the {@link EMAIndicator} of {@code this}
   */
  public EMAIndicator ema(final int barCount) {
    return new EMAIndicator(this, barCount);
  }


  public DoubleEMAIndicator doubleEMA(final int barCount) {
    return new DoubleEMAIndicator(this, barCount);
  }


  public TripleEMAIndicator tripleEma(final int barCount) {
    return new TripleEMAIndicator(this, barCount);
  }


  public ZLEMAIndicator zlema(final int barCount) {
    return new ZLEMAIndicator(this, barCount);
  }


  public MMAIndicator mma(final int barCount) {
    return new MMAIndicator(this, barCount);
  }


  public HMAIndicator hma(final int barCount) {
    return new HMAIndicator(this, barCount);
  }


  public LWMAIndicator lwma(final int barCount) {
    return new LWMAIndicator(this, barCount);
  }


  public WMAIndicator wma(final int barCount) {
    return new WMAIndicator(this, barCount);
  }


  public KAMAIndicator kama(
      final int barCountEffectiveRatio,
      final int barCountFast,
      final int barCountSlow
  ) {
    return new KAMAIndicator(this, barCountEffectiveRatio, barCountFast, barCountSlow);
  }


  /**
   * @param barCount the time frame
   *
   * @return the {@link StandardDeviationIndicator} of {@code this}
   */
  public StandardDeviationIndicator stddev(final int barCount) {
    return new StandardDeviationIndicator(this, barCount);
  }


  public StandardErrorIndicator stderr(final int barCount) {
    return new StandardErrorIndicator(this, barCount);
  }


  public MeanDeviationIndicator meanDeviation(final int barCount) {
    return new MeanDeviationIndicator(this, barCount);
  }


  public VarianceIndicator variance(final int barCount) {
    return new VarianceIndicator(this, barCount);
  }


  public SigmaIndicator sigma(final int barCount) {
    return new SigmaIndicator(this, barCount);
  }


  public CovarianceIndicator covariance(final NumericIndicator indicator, final int barCount) {
    return new CovarianceIndicator(this, indicator, barCount);
  }


  /**
   * @param barCount the time frame
   *
   * @return the {@link HighestValueIndicator} of {@code this}
   */
  public HighestValueIndicator highest(final int barCount) {
    return new HighestValueIndicator(this, barCount);
  }


  /**
   * @param barCount the time frame
   *
   * @return the {@link LowestValueIndicator} of {@code this}
   */
  public LowestValueIndicator lowest(final int barCount) {
    return new LowestValueIndicator(this, barCount);
  }


  /**
   * @param barCount the time frame
   *
   * @return the {@link PreviousNumericValueIndicator} of {@code this}
   */
  public PreviousNumericValueIndicator previous(final int barCount) {
    return new PreviousNumericValueIndicator(this, barCount);
  }


  /**
   * @return the {@link PreviousNumericValueIndicator} of {@code this} with
   *     {@code barCount=1}
   */
  public PreviousNumericValueIndicator previous() {
    return previous(1);
  }


  /**
   * Returns sum of last barCount values
   *
   * @param barCount how many values sum up
   *
   * @return sum indicator
   */
  public RunningTotalIndicator runningTotal(final int barCount) {
    return new RunningTotalIndicator(this, barCount);
  }


  /**
   * Whether cross over occured in last 5 bars.
   *
   * @param other the other indicator
   *
   * @return the {@link CrossIndicator} of {@code this} and {@code other}
   */
  public CrossIndicator crossedOver(final NumericIndicator other) {
    return crossedOver(other, 1);
  }


  /**
   * @param other the other indicator
   * @param barCount test whether cross occured within last barCount
   *
   * @return the {@link CrossIndicator} of {@code this} and {@code other}
   */
  public CrossIndicator crossedOver(final NumericIndicator other, final int barCount) {
    return new CrossIndicator(this, other, barCount);
  }


  /**
   * @param n the other number
   *
   * @return the {@link CrossIndicator} of {@code this} and {@code n}
   */
  public CrossIndicator crossedOver(final Number n) {
    return crossedOver(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return the {@link CrossIndicator} of {@code this} and
   *     {@code other}
   */
  public CrossIndicator crossedUnder(final NumericIndicator other) {
    return new CrossIndicator(other, this, 1);
  }


  /**
   * @param n the other number
   *
   * @return he current comparison of {@code this} and {@code n}
   */
  public CrossIndicator crossedUnder(final Number n) {
    return crossedUnder(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return he current comparison of {@code this} and {@code other}
   */
  public boolean isGreaterThan(final NumericIndicator other) {
    return getValue().isGreaterThan(other.getValue());
  }


  /**
   * @param n the other number
   *
   * @return the current comparison of {@code this} and {@code n}
   */
  public boolean isGreaterThan(final Number n) {
    return isGreaterThan(createConstant(n));
  }


  public OverIndicatorRule isGreaterThanRule(final NumericIndicator other) {
    return new OverIndicatorRule(this, other);
  }


  public OverIndicatorRule isGreaterThanRule(final Number n) {
    return isGreaterThanRule(createConstant(n));
  }


  /**
   * @param other the other indicator
   *
   * @return he current comparison of {@code this} and {@code other}
   */
  public boolean isLessThan(final NumericIndicator other) {
    return getValue().isLessThan(other.getValue());
  }


  public UnderIndicatorRule isLessThanRule(final NumericIndicator other) {
    return new UnderIndicatorRule(this, other);
  }


  public UnderIndicatorRule isLessThanRule(final Number n) {
    return isLessThanRule(createConstant(n));
  }


  /**
   * @param n the other number
   *
   * @return the {@link UnderIndicatorRule} of {@code this} and {@code n}
   */
  public boolean isLessThan(final Number n) {
    return isLessThan(createConstant(n));
  }


  private ConstantNumericIndicator createConstant(final Number n) {
    return new ConstantNumericIndicator(getNumFactory().numOf(n));
  }


  public SimpleLinearRegressionIndicator simpleLinearRegression(final int barCount) {
    return new SimpleLinearRegressionIndicator(this, barCount);
  }


  @Override
  public Num getValue() {
    log.debug("{}", this);

    if (this.value == null) {
      throw new IllegalStateException(
          "Calling getValue() on uninitialized instance %s. Did you register it into IndicatorContext?".formatted(
              getClass().getSimpleName()));
    }

    return this.value;
  }


  @Override
  public final void onBar(final Bar bar) {
    if (bar.beginTime().isAfter(this.currentBeginTime)) {
      updateState(bar);
      this.currentBeginTime = bar.beginTime();
    }
  }


  /**
   * Called by this class when it is the right time to update internal state.
   *
   * @param bar new {@link Bar} received
   */
  protected abstract void updateState(Bar bar);
}

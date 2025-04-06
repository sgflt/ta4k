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
package org.ta4j.core.indicators.numeric.operation;

import java.util.Objects;
import java.util.function.BinaryOperator;

import org.ta4j.core.api.series.Bar;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.Num;

/**
 * Objects of this class defer evaluation of an arithmetic operation.
 *
 * <p>
 * This is a lightweight version of the
 * {@link CombineIndicator CombineIndicator};
 * it doesn't cache.
 */
public class BinaryOperation extends NumericIndicator {

  private final BinaryOperator<Num> operator;
  private final NumericIndicator left;
  private final NumericIndicator right;


  private BinaryOperation(
      final BinaryOperator<Num> operator,
      final NumericIndicator left,
      final NumericIndicator right
  ) {
    super(left.getNumFactory());
    this.operator = operator;
    this.left = Objects.requireNonNull(left);
    this.right = Objects.requireNonNull(right);
  }


  /**
   * Returns an {@code Indicator} whose value is {@code (left + right)}.
   *
   * @param left
   * @param right
   *
   * @return {@code left + right}, rounded as necessary
   *
   * @see Num#plus
   */
  public static BinaryOperation sum(final NumericIndicator left, final NumericIndicator right) {
    return new BinaryOperation(Num::plus, left, right);
  }


  /**
   * Returns an {@code Indicator} whose value is {@code (left - right)}.
   *
   * @param left
   * @param right
   *
   * @return {@code left - right}, rounded as necessary
   *
   * @see Num#minus
   */
  public static BinaryOperation difference(final NumericIndicator left, final NumericIndicator right) {
    return new BinaryOperation(Num::minus, left, right);
  }


  /**
   * Returns an {@code Indicator} whose value is {@code (left * right)}.
   *
   * @param left
   * @param right
   *
   * @return {@code left * right}, rounded as necessary
   *
   * @see Num#multipliedBy
   */
  public static BinaryOperation product(final NumericIndicator left, final NumericIndicator right) {
    return new BinaryOperation(Num::multipliedBy, left, right);
  }


  /**
   * Returns an {@code Indicator} whose value is {@code (left / right)}.
   *
   * @param left
   * @param right
   *
   * @return {@code left / right}, rounded as necessary
   *
   * @see Num#dividedBy
   */
  public static BinaryOperation quotient(final NumericIndicator left, final NumericIndicator right) {
    return new BinaryOperation(Num::dividedBy, left, right);
  }


  /**
   * Returns the minimum of {@code left} and {@code right} as an
   * {@code Indicator}.
   *
   * @param left
   * @param right
   *
   * @return the {@code Indicator} whose value is the smaller of {@code left} and
   *     {@code right}. If they are equal, {@code left} is returned.
   *
   * @see Num#min
   */
  public static BinaryOperation min(final NumericIndicator left, final NumericIndicator right) {
    return new BinaryOperation(Num::min, left, right);
  }


  /**
   * Returns the maximum of {@code left} and {@code right} as an
   * {@code Indicator}.
   *
   * @param left
   * @param right
   *
   * @return the {@code Indicator} whose value is the greater of {@code left} and
   *     {@code right}. If they are equal, {@code left} is returned.
   *
   * @see Num#max
   */
  public static BinaryOperation max(final NumericIndicator left, final NumericIndicator right) {
    return new BinaryOperation(Num::max, left, right);
  }


  private Num calculate() {
    final Num n1 = this.left.getValue();
    final Num n2 = this.right.getValue();
    return this.operator.apply(n1, n2);
  }


  @Override
  public void updateState(final Bar bar) {
    this.left.onBar(bar);
    this.right.onBar(bar);
    this.value = calculate();
  }


  @Override
  public boolean isStable() {
    return this.left.isStable() && this.right.isStable();
  }


  @Override
  public String toString() {
    return String.format("BI<%s, %s> => %s", this.left, this.right, getValue());
  }
}

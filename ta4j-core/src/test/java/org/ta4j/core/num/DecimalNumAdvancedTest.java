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
package org.ta4j.core.num;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.MathContext;
import java.math.RoundingMode;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DecimalNumAdvancedTest {

  private static final MathContext TEST_CONTEXT = new MathContext(5, RoundingMode.HALF_UP);


  @Test
  @DisplayName("Test logarithmic operations")
  void shouldCalculateLogarithms() {
    final var one = DecimalNum.valueOf("1", TEST_CONTEXT);
    final var e = DecimalNum.valueOf(Math.E, TEST_CONTEXT);

    // Natural log tests
    assertThat(one.log()).hasToString("0");
    assertThat(e.log().getDelegate().doubleValue()).isCloseTo(1.0, Offset.offset(1e-10));

    // Log of negative number should return NaN
    final var minusOne = DecimalNum.valueOf("-1", TEST_CONTEXT);
    assertThat(minusOne.log().isNaN()).isTrue();

    // Log of zero should return NaN
    final var zero = DecimalNum.valueOf("0", TEST_CONTEXT);
    assertThat(zero.log().isNaN()).isTrue();
  }


  @Test
  @DisplayName("Test floor and ceiling operations")
  void shouldCalculateFloorAndCeiling() {
    final var num1 = DecimalNum.valueOf("3.7", TEST_CONTEXT);
    final var num2 = DecimalNum.valueOf("-3.7", TEST_CONTEXT);

    // Floor tests
    assertThat(num1.floor()).hasToString("3");
    assertThat(num2.floor()).hasToString("-4");

    // Ceiling tests
    assertThat(num1.ceil()).hasToString("4");
    assertThat(num2.ceil()).hasToString("-3");
  }


  @Test
  @DisplayName("Test min and max operations")
  void shouldFindMinAndMax() {
    final var num1 = DecimalNum.valueOf("3.7", TEST_CONTEXT);
    final var num2 = DecimalNum.valueOf("-3.7", TEST_CONTEXT);
    final var num3 = DecimalNum.valueOf("5.2", TEST_CONTEXT);

    // Min tests
    assertThat(num1.min(num2)).isEqualTo(num2);
    assertThat(num1.min(num3)).isEqualTo(num1);

    // Max tests
    assertThat(num1.max(num2)).isEqualTo(num1);
    assertThat(num1.max(num3)).isEqualTo(num3);

    // Min/Max with NaN should return NaN
    assertThat(num1.min(NaN.NaN).isNaN()).isTrue();
    assertThat(num1.max(NaN.NaN).isNaN()).isTrue();
  }


  @Test
  @DisplayName("Test absolute value and negation")
  void shouldCalculateAbsoluteAndNegation() {
    final var num1 = DecimalNum.valueOf("3.7", TEST_CONTEXT);
    final var num2 = DecimalNum.valueOf("-3.7", TEST_CONTEXT);
    final var zero = DecimalNum.valueOf("0", TEST_CONTEXT);

    // Absolute value tests
    assertThat(num1.abs()).isEqualTo(num1);
    assertThat(num2.abs()).isEqualTo(num1);
    assertThat(zero.abs()).isEqualTo(zero);

    // Negation tests
    assertThat(num1.negate()).isEqualTo(num2);
    assertThat(num2.negate()).isEqualTo(num1);
    assertThat(zero.negate()).isEqualTo(zero);
  }


  @Test
  @DisplayName("Test remainder operation")
  void shouldCalculateRemainder() {
    final var num1 = DecimalNum.valueOf("10", TEST_CONTEXT);
    final var num2 = DecimalNum.valueOf("3", TEST_CONTEXT);
    final var expectedRemainder = DecimalNum.valueOf("1", TEST_CONTEXT);

    assertThat(num1.remainder(num2)).isEqualTo(expectedRemainder);

    // Remainder with NaN should return NaN
    assertThat(num1.remainder(NaN.NaN).isNaN()).isTrue();
  }


  @Test
  @DisplayName("Test precision handling in arithmetic")
  void shouldMaintainPrecisionInArithmetic() {
    final var num1 = DecimalNum.valueOf("1.23456789", TEST_CONTEXT);
    final var num2 = DecimalNum.valueOf("9.87654321", TEST_CONTEXT);

    // Test that arithmetic operations maintain precision
    final var sum = num1.plus(num2);
    final var difference = num2.minus(num1);
    final var product = num1.multipliedBy(num2);
    final var quotient = num2.dividedBy(num1);
    final var sqrt = num2.sqrt();
    final var pow = num2.pow(5);
    final var log = num2.log();
    final var abs = num2.abs();

    assertThat(((DecimalNum) sum).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) difference).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) product).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) quotient).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) sqrt).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) pow).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) log).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
    assertThat(((DecimalNum) abs).getDelegate().precision()).isLessThanOrEqualTo(TEST_CONTEXT.getPrecision());
  }
}

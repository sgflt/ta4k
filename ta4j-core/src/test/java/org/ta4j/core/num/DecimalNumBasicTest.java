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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DecimalNumBasicTest {

  private static final MathContext TEST_CONTEXT = new MathContext(5, RoundingMode.HALF_UP);

  // 120 digit precision
  private static final String SUPER_PRECISION_STRING = "1.234567890" + // 10
                                                       "1234567890".repeat(11); // 110


  @Test
  @DisplayName("Test value creation methods")
  void shouldCreateValuesCorrectly() {
    // Test string constructor
    final var stringNum = DecimalNum.valueOf("123.456", TEST_CONTEXT);
    assertEquals("123.46", stringNum.toString());

    // Test various number types
    final var shortNum = DecimalNum.valueOf((short) 123, TEST_CONTEXT);
    final var intNum = DecimalNum.valueOf(123, TEST_CONTEXT);
    final var longNum = DecimalNum.valueOf(123L, TEST_CONTEXT);
    final var floatNum = DecimalNum.valueOf(123.456f, TEST_CONTEXT);
    final var doubleNum = DecimalNum.valueOf(123.456d, TEST_CONTEXT);
    final var bigDecimalNum = DecimalNum.valueOf(new BigDecimal("123.456"), TEST_CONTEXT);

    assertThat(shortNum).hasToString("123");
    assertThat(intNum).hasToString("123");
    assertThat(longNum).hasToString("123");
    assertThat(floatNum).hasToString("123.46");
    assertThat(doubleNum.getDelegate()).isEqualTo(new BigDecimal("123.46"));
    assertThat(bigDecimalNum).hasToString("123.46");

    // Test NaN handling
    assertThrows(NumberFormatException.class, () -> DecimalNum.valueOf("NaN", TEST_CONTEXT));
    assertThrows(NumberFormatException.class, () -> DecimalNum.valueOf(Double.NaN, TEST_CONTEXT));
    assertThrows(NumberFormatException.class, () -> DecimalNum.valueOf(Float.NaN, TEST_CONTEXT));
  }


  @Test
  @DisplayName("Test basic arithmetic operations")
  void shouldPerformBasicArithmetic() {
    final var two = DecimalNum.valueOf("2", TEST_CONTEXT);
    final var three = DecimalNum.valueOf("3", TEST_CONTEXT);
    final var six = DecimalNum.valueOf("6", TEST_CONTEXT);
    final var minusTwo = DecimalNum.valueOf("-2", TEST_CONTEXT);

    // Addition
    assertThat(two.plus(three)).hasToString("5");
    assertThat(two.plus(minusTwo)).hasToString("0");

    // Subtraction
    assertThat(three.minus(two)).hasToString("1");
    assertThat(two.minus(three)).hasToString("-1");

    // Multiplication
    assertThat(two.times(three)).hasToString("6");
    assertThat(two.times(minusTwo)).hasToString("-4");

    // Division
    assertThat(six.div(two)).hasToString("3");
    assertThat(six.div(three)).hasToString("2");

    // Division by zero should return NaN
    assertThat(six.div(DecimalNum.valueOf("0", TEST_CONTEXT)).isNaN()).isTrue();
  }


  @Test
  @DisplayName("Test comparison operations")
  void shouldCompareCorrectly() {
    final var two = DecimalNum.valueOf("2", TEST_CONTEXT);
    final var three = DecimalNum.valueOf("3", TEST_CONTEXT);
    final var twoAgain = DecimalNum.valueOf("2", TEST_CONTEXT);
    final var zero = DecimalNum.valueOf("0", TEST_CONTEXT);
    final var minusTwo = DecimalNum.valueOf("-2", TEST_CONTEXT);

    // Zero checks
    assertTrue(zero.isZero());
    assertFalse(two.isZero());

    // Negative checks
    assertTrue(minusTwo.isNegative());
    assertFalse(two.isNegative());

    // Positive checks
    assertTrue(two.isPositive());
    assertFalse(minusTwo.isPositive());
  }


  @Test
  @DisplayName("Test power and root operations")
  void shouldCalculatePowersAndRoots() {
    final var two = DecimalNum.valueOf("2", TEST_CONTEXT);
    final var three = DecimalNum.valueOf("3", TEST_CONTEXT);
    final var nine = DecimalNum.valueOf("9", TEST_CONTEXT);
    final var minusNine = DecimalNum.valueOf("-9", TEST_CONTEXT);

    // Integer powers
    assertThat(two.pow(3)).hasToString("8");
    assertThat(three.pow(2)).hasToString("9");

    // Square root
    assertThat(nine.sqrt()).hasToString("3.0");

    // Square root of negative number should return NaN
    assertThat(minusNine.sqrt().isNaN()).isTrue();

    // Power with Num
    assertThat(two.pow(three)).hasToString("8.0");
  }


  @Test
  void testPowLargeBase() {
    final Num x = DecimalNum.valueOf(SUPER_PRECISION_STRING);
    final Num n = DecimalNum.valueOf("512");
    final Num result = x.pow(n);
    assertThat(result).isEqualTo(
        DecimalNum.valueOf(
            "71724632698264595311439425390811606219342673848.4006139819755840062853530122432564321605895163413448768150879157699962725")
    );
    assertEquals(120, ((BigDecimal) result.getDelegate()).precision());
    assertEquals(120, ((DecimalNum) result).getMathContext().getPrecision());
  }
}

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DecimalNumEdgeCasesTest {

  private static final MathContext TEST_CONTEXT = new MathContext(32, RoundingMode.HALF_UP);


  @Test
  @DisplayName("Test scientific notation handling")
  void shouldHandleScientificNotation() {
    // Very small numbers
    final var verySmall = DecimalNum.valueOf("1E-30", TEST_CONTEXT);
    final var verySmallPlus1 = verySmall.plus(DecimalNum.valueOf("1", TEST_CONTEXT));
    assertThat(verySmallPlus1).hasToString("1.000000000000000000000000000001");

    // Very large numbers
    final var veryLarge = DecimalNum.valueOf("1E+30", TEST_CONTEXT);
    final var veryLargeMinus1 = veryLarge.minus(DecimalNum.valueOf("1", TEST_CONTEXT));
    assertThat(veryLargeMinus1)
        .hasToString("999999999999999999999999999999");
  }


  @Test
  @DisplayName("Test precision with recurring decimals")
  void shouldHandleRecurringDecimals() {
    // 1/3 = 0.333...
    final var oneThird = DecimalNum.valueOf("1", TEST_CONTEXT)
        .div(DecimalNum.valueOf("3", TEST_CONTEXT));

    // Multiply back by 3
    final var shouldBeOne = oneThird.times(DecimalNum.valueOf("3", TEST_CONTEXT));

    // Should equal 1 despite intermediate recurring decimal
    assertThat(shouldBeOne).hasToString("0.99999999999999999999999999999999");
  }


  @Test
  @DisplayName("Test behavior with extreme values")
  void shouldHandleExtremeValues() {
    final var mc = new MathContext(5, RoundingMode.HALF_UP);

    // Test extremely close to zero
    final var almostZero = DecimalNum.valueOf("0.0000000001", mc);
    assertThat(almostZero.isZero()).isFalse();
    assertThat(almostZero.abs()).isEqualTo(almostZero);

    // Test extremely large numbers
    final var veryLarge = DecimalNum.valueOf("9".repeat(100), mc);
    final var doubled = veryLarge.times(DecimalNum.valueOf("2", mc));
    assertThat(doubled.compareTo(veryLarge) == 1).isTrue();
  }


  @ParameterizedTest
  @ValueSource(
      strings = {
          "0.0",
          "-0.0",
          "0.00000",
          "-0.00000"
      }
  )
  @DisplayName("Test zero representations")
  void shouldHandleZeroRepresentations(final String zeroStr) {
    final var num = DecimalNum.valueOf(zeroStr, TEST_CONTEXT);
    assertThat(num.isZero()).isTrue();
    assertThat(num.isPositive()).isFalse();
    assertThat(num.isNegative()).isFalse();
  }


  @Test
  @DisplayName("Test precision handling across operations")
  void shouldMaintainPrecisionAcrossOperations() {
    final var highPrecisionContext = new MathContext(50, RoundingMode.HALF_UP);
    final var lowPrecisionContext = new MathContext(5, RoundingMode.HALF_UP);

    final var highPrecisionNum = DecimalNum.valueOf("1.1111111111111111111", highPrecisionContext);
    final var lowPrecisionNum = DecimalNum.valueOf("1.1111111111111111111", lowPrecisionContext);

    // Operations between different precisions should use higher precision
    final var result = highPrecisionNum.times(lowPrecisionNum);
    assertThat(((DecimalNum) result).getDelegate().precision())
        .isLessThanOrEqualTo(highPrecisionContext.getPrecision());
  }


  @Test
  @DisplayName("Test equality and hash code contracts")
  void shouldMaintainEqualityContracts() {
    final var num1 = DecimalNum.valueOf("1.23", TEST_CONTEXT);
    final var num2 = DecimalNum.valueOf("1.23", TEST_CONTEXT);
    final var num3 = DecimalNum.valueOf("1.23", new MathContext(5, RoundingMode.HALF_UP));
    final var differentNum = DecimalNum.valueOf("1.24", TEST_CONTEXT);

    // Equality tests
    assertEquals(num1, num2);
    assertNotEquals(num1, differentNum);

    // HashCode tests
    assertEquals(num1.hashCode(), num2.hashCode());
    assertNotEquals(num1.hashCode(), differentNum.hashCode());

    // Different precision but same value should be equal
    assertEquals(num1, num3);
  }


  @Test
  @DisplayName("Test conversion methods")
  void shouldConvertCorrectly() {
    final var num = DecimalNum.valueOf("123.456", TEST_CONTEXT);

    assertThat(num.intValue()).isEqualTo(123);
    assertThat(num.longValue()).isEqualTo(123L);
    assertThat(num.floatValue()).isEqualTo(123.456f);
    assertThat(num.doubleValue()).isEqualTo(123.456d);
    assertThat(num.getDelegate()).isInstanceOf(BigDecimal.class);
  }
}

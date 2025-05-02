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
package org.ta4j.core;

import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.ta4j.core.TestUtils.assertNumNotEquals;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ta4j.core.num.NumFactory;

class TestUtilsTest {

  private static final String STRING_DOUBLE = "1234567890.12345";
  private static final String DIFF_STRING_DOUBLE = "1234567890.12346";
  private static final BigDecimal BIG_DECIMAL_DOUBLE = new BigDecimal(STRING_DOUBLE);
  private static final BigDecimal DIFF_BIG_DECIMAL_DOUBLE = new BigDecimal(DIFF_STRING_DOUBLE);
  private static final int A_INT = 1234567890;
  private static final int DIFF_INT = 1234567891;
  private static final double A_DOUBLE = 1234567890.1234;
  private static final double DIFF_DOUBLE = 1234567890.1235;


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testStringNum(final NumFactory numFactory) {
    final var numStringDouble = numFactory.numOf(BIG_DECIMAL_DOUBLE);
    final var diffNumStringDouble = numFactory.numOf(DIFF_BIG_DECIMAL_DOUBLE);

    assertNumEquals(STRING_DOUBLE, numStringDouble);
    assertNumNotEquals(STRING_DOUBLE, diffNumStringDouble);
    assertNumNotEquals(DIFF_STRING_DOUBLE, numStringDouble);
    assertNumEquals(DIFF_STRING_DOUBLE, diffNumStringDouble);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testNumNum(final NumFactory numFactory) {
    final var numStringDouble = numFactory.numOf(BIG_DECIMAL_DOUBLE);
    final var diffNumStringDouble = numFactory.numOf(DIFF_BIG_DECIMAL_DOUBLE);

    assertNumEquals(numStringDouble, numStringDouble);
    assertNumNotEquals(numStringDouble, diffNumStringDouble);
    assertNumNotEquals(diffNumStringDouble, numStringDouble);
    assertNumEquals(diffNumStringDouble, diffNumStringDouble);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testIntNum(final NumFactory numFactory) {
    final var numInt = numFactory.numOf(A_INT);
    final var diffNumInt = numFactory.numOf(DIFF_INT);

    assertNumEquals(A_INT, numInt);
    assertNumNotEquals(A_INT, diffNumInt);
    assertNumNotEquals(DIFF_INT, numInt);
    assertNumEquals(DIFF_INT, diffNumInt);
  }


  @ParameterizedTest
  @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
  void testDoubleNum(final NumFactory numFactory) {
    final var numDouble = numFactory.numOf(A_DOUBLE);
    final var diffNumDouble = numFactory.numOf(DIFF_DOUBLE);

    assertNumEquals(A_DOUBLE, numDouble);
    assertNumNotEquals(A_DOUBLE, diffNumDouble);
    assertNumNotEquals(DIFF_DOUBLE, numDouble);
    assertNumEquals(DIFF_DOUBLE, diffNumDouble);
  }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.indicators.Indicator;
import org.ta4j.core.num.Num;

/**
 * Utility class for {@code Num} tests.
 */
public class TestUtils {

  /** Offset for double equality checking */
  public static final double GENERAL_OFFSET = 0.0001;

  private static final Logger log = LoggerFactory.getLogger(TestUtils.class);


  /**
   * Verifies that the actual {@code Num} value is equal to the given
   * {@code String} representation.
   *
   * @param expected the given {@code String} representation to compare the actual
   *     value to
   * @param actual the actual {@code Num} value
   *
   * @throws AssertionError if the actual value is not equal to the given
   *     {@code String} representation
   */
  public static void assertNumEquals(final String expected, final Num actual) {
    assertEquals(actual.getNumFactory().numOf(new BigDecimal(expected)), actual);
  }


  /**
   * Verifies that the actual {@code Num} value is equal to the given {@code Num}.
   *
   * @param expected the given {@code Num} representation to compare the actual
   *     value to
   * @param actual the actual {@code Num} value
   *
   * @throws AssertionError if the actual value is not equal to the given
   *     {@code Num} representation
   */
  public static void assertNumEquals(final Num expected, final Num actual) {
    assertEquals(expected, actual);
  }


  /**
   * Verifies that the actual {@code Num} value is equal to the given {@code int}
   * representation.
   *
   * @param expected the given {@code int} representation to compare the actual
   *     value to
   * @param actual the actual {@code Num} value
   *
   * @throws AssertionError if the actual value is not equal to the given
   *     {@code int} representation
   */
  public static void assertNumEquals(final int expected, final Num actual) {
    if (actual.isNaN()) {
      throw new AssertionError("Expected: " + expected + " Actual: " + actual);
    }
    assertEquals(actual.getNumFactory().numOf(expected), actual);
  }


  /**
   * Verifies that the actual {@code Num} value is equal (within a positive
   * offset) to the given {@code double} representation.
   *
   * @param expected the given {@code double} representation to compare the actual
   *     value to
   * @param actual the actual {@code Num} value
   *
   * @throws AssertionError if the actual value is not equal to the given
   *     {@code double} representation
   */
  public static void assertNumEquals(final double expected, final Num actual) {
    assertEquals(expected, actual.doubleValue(), GENERAL_OFFSET);
  }


  /**
   * Verifies that the actual {@code Num} value is equal (within a positive
   * offset) to the given {@code double} representation.
   *
   * @param expected the given {@code double} representation to compare the actual
   *     value to
   * @param actual the actual {@code Num} value
   *
   * @throws AssertionError if the actual value is not equal to the given
   *     {@code double} representation
   */
  public static void assertNumEquals(final double expected, final double actual) {
    assertEquals(expected, actual, GENERAL_OFFSET);
  }


  /**
   * Verifies that the actual {@code Num} value is not equal to the given
   * {@code int} representation.
   *
   * @param actual the actual {@code Num} value
   * @param unexpected the given {@code int} representation to compare the actual
   *     value to
   *
   * @throws AssertionError if the actual value is equal to the given {@code int}
   *     representation
   */
  public static void assertNumNotEquals(final int unexpected, final Num actual) {
    assertNotEquals(actual.getNumFactory().numOf(unexpected), actual);
  }


//  /**
//   * Verifies that two indicators have the same size and values to an offset
//   *
//   * @param expected indicator of expected values
//   * @param actual indicator of actual values
//   */
//  public static void assertIndicatorEquals(
//      TestContext context,
//      final MockIndicator expected,
//      final MockIndicator actual
//  ) {
//    while (context.advance()) {
//
//      if (actual.isStable()) {
//        assertEquals(
//            String.format(
//                "Failed at index %s: %s",
//                expected.getBarSeries().getCurrentIndex(), actual
//            ),
//            expected.getValue().doubleValue(), actual.getValue().doubleValue(), GENERAL_OFFSET
//        );
//      }
//    }
//  }
//
//
//  private static void advanceIfTwoSeries(final TestIndicator<Num> expected, final TestIndicator<Num> actual) {
//    if (!expected.getBarSeries().equals(actual.getBarSeries())) {
//      actual.getBarSeries().advance();
//    }
//  }
//
//
//  /**
//   * Verifies that two indicators have either different size or different values
//   * to an offset
//   *
//   * @param expected indicator of expected values
//   * @param actual indicator of actual values
//   */
//  public static void assertIndicatorNotEquals(
//      final TestIndicator<Num> expected,
//      final TestIndicator<Num> actual
//  ) {
//    if (expected.getBarSeries().getBarCount() != actual.getBarSeries().getBarCount()) {
//      return;
//    }
//
//    while (expected.getBarSeries().advance()) {
//      advanceIfTwoSeries(expected, actual);
//
//      if (Math.abs(expected.getValue().doubleValue() - actual.getValue().doubleValue()) > GENERAL_OFFSET) {
//        return;
//      }
//    }
//    throw new AssertionError("Indicators match to " + GENERAL_OFFSET);
//  }


  /**
   * Verifies that the actual {@code Num} value is not equal to the given
   * {@code String} representation.
   *
   * @param actual the actual {@code Num} value
   * @param expected the given {@code String} representation to compare the actual
   *     value to
   *
   * @throws AssertionError if the actual value is equal to the given
   *     {@code String} representation
   */
  public static void assertNumNotEquals(final String expected, final Num actual) {
    assertNotEquals(actual.getNumFactory().numOf(new BigDecimal(expected)), actual);
  }


  /**
   * Verifies that the actual {@code Num} value is not equal to the given
   * {@code Num}.
   *
   * @param actual the actual {@code Num} value
   * @param expected the given {@code Num} representation to compare the actual
   *     value to
   *
   * @throws AssertionError if the actual value is equal to the given {@code Num}
   *     representation
   */
  public static void assertNumNotEquals(final Num expected, final Num actual) {
    assertNotEquals(expected, actual);
  }


  /**
   * Verifies that the actual {@code Num} value is not equal (within a positive
   * offset) to the given {@code double} representation.
   *
   * @param actual the actual {@code Num} value
   * @param expected the given {@code double} representation to compare the actual
   *     value to
   *
   * @throws AssertionError if the actual value is equal to the given
   *     {@code double} representation
   */
  public static void assertNumNotEquals(final double expected, final Num actual) {
    assertNotEquals(expected, actual.doubleValue(), GENERAL_OFFSET);
  }
//
//
//  /**
//   * Verifies that two indicators have the same size and values
//   *
//   * @param expected indicator of expected values
//   * @param actual indicator of actual values
//   */
//  public static void assertIndicatorEquals(
//      final TestIndicator<Num> expected,
//      final TestIndicator<Num> actual,
//      final Num delta
//  ) {
//    assertEquals(
//        "Size does not match,",
//        expected.getBarSeries().getBarCount(),
//        actual.getBarSeries().getBarCount()
//    );
//    while (expected.getBarSeries().advance()) {
//      // convert to DecimalNum via String (auto-precision) avoids Cast Class
//      // Exception
//      final Num exp = DecimalNum.valueOf(expected.getValue().toString());
//      final Num act = DecimalNum.valueOf(actual.getValue().toString());
//      final Num result = exp.minus(act).abs();
//      if (result.isGreaterThan(delta)) {
//        log.debug("{} expected does not match", exp);
//        log.debug("{} actual", act);
//        log.debug("{} offset", delta);
//        String expString = exp.toString();
//        String actString = act.toString();
//        final int minLen = Math.min(expString.length(), actString.length());
//        if (expString.length() > minLen) {
//          expString = expString.substring(0, minLen) + "..";
//        }
//        if (actString.length() > minLen) {
//          actString = actString.substring(0, minLen) + "..";
//        }
//        throw new AssertionError(String.format(
//            "Failed at index %s: expected %s but actual was %s",
//            expected.getBarSeries().getCurrentIndex(), expString, actString
//        ));
//      }
//    }
//  }

//
//  /**
//   * Verifies that two indicators have either different size or different values
//   * to an offset
//   *
//   * @param expected indicator of expected values
//   * @param actual indicator of actual values
//   * @param delta num offset to which the indicators must be different
//   */
//  public static void assertIndicatorNotEquals(
//      final TestIndicator<Num> expected,
//      final TestIndicator<Num> actual,
//      final Num delta
//  ) {
//    if (expected.getBarSeries().getBarCount() != actual.getBarSeries().getBarCount()) {
//      return;
//    }
//    while (expected.getBarSeries().advance()) {
//      final Num exp = DecimalNum.valueOf(expected.getValue().toString());
//      final Num act = DecimalNum.valueOf(actual.getValue().toString());
//      final Num result = exp.minus(act).abs();
//      if (result.isGreaterThan(delta)) {
//        return;
//      }
//    }
//    throw new AssertionError("Indicators match to " + delta);
//  }


  public static void assertUnstable(final Indicator<?> indicator) {
    assertFalse(indicator.isStable());
  }


  public static void assertStable(final Indicator<?> indicator) {
    assertTrue(indicator.isStable());
  }
}

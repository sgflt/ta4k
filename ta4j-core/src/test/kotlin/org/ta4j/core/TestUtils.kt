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
package org.ta4j.core

import java.math.BigDecimal
import org.assertj.core.api.Assertions
import org.assertj.core.data.Offset
import org.junit.Assert
import org.ta4j.core.api.Indicator
import org.ta4j.core.num.Num

/**
 * Utility class for `Num` tests.
 */
object TestUtils {
    /** Offset for double equality checking  */
    const val GENERAL_OFFSET: Double = 0.0001

    /**
     * Verifies that the actual `Num` value is equal to the given
     * `String` representation.
     *
     * @param expected the given `String` representation to compare the actual
     * value to
     * @param actual the actual `Num` value
     *
     * @throws AssertionError if the actual value is not equal to the given
     * `String` representation
     */
    @JvmStatic
    fun assertNumEquals(expected: String, actual: Num) {
        Assert.assertEquals(actual.numFactory.numOf(BigDecimal(expected)), actual)
    }


    /**
     * Verifies that the actual `Num` value is equal to the given `Num`.
     *
     * @param expected the given `Num` representation to compare the actual
     * value to
     * @param actual the actual `Num` value
     *
     * @throws AssertionError if the actual value is not equal to the given
     * `Num` representation
     */
    @JvmStatic
    fun assertNumEquals(expected: Num?, actual: Num?) {
        Assert.assertEquals(expected, actual)
    }


    /**
     * Verifies that the actual `Num` value is equal to the given `int`
     * representation.
     *
     * @param expected the given `int` representation to compare the actual
     * value to
     * @param actual the actual `Num` value
     *
     * @throws AssertionError if the actual value is not equal to the given
     * `int` representation
     */
    @JvmStatic
    fun assertNumEquals(expected: Int, actual: Num) {
        if (actual.isNaN) {
            throw AssertionError("Expected: " + expected + " Actual: " + actual)
        }
        Assert.assertEquals(actual.numFactory.numOf(expected), actual)
    }


    /**
     * Verifies that the actual `Num` value is equal (within a positive
     * offset) to the given `double` representation.
     *
     * @param expected the given `double` representation to compare the actual
     * value to
     * @param actual the actual `Num` value
     *
     * @throws AssertionError if the actual value is not equal to the given
     * `double` representation
     */
    @JvmStatic
    fun assertNumEquals(expected: Double, actual: Num) {
        assertNumEquals(expected, actual.doubleValue())
    }


    /**
     * Verifies that the actual `Num` value is equal (within a positive
     * offset) to the given `double` representation.
     *
     * @param expected the given `double` representation to compare the actual
     * value to
     * @param actual the actual `Num` value
     *
     * @throws AssertionError if the actual value is not equal to the given
     * `double` representation
     */
    @JvmStatic
    fun assertNumEquals(expected: Double, actual: Double) {
        Assertions.assertThat(actual).isCloseTo(expected, Offset.offset<Double?>(GENERAL_OFFSET))
    }


    /**
     * Verifies that the actual `Num` value is not equal to the given
     * `int` representation.
     *
     * @param actual the actual `Num` value
     * @param unexpected the given `int` representation to compare the actual
     * value to
     *
     * @throws AssertionError if the actual value is equal to the given `int`
     * representation
     */
    @JvmStatic
    fun assertNumNotEquals(unexpected: Int, actual: Num) {
        Assert.assertNotEquals(actual.numFactory.numOf(unexpected), actual)
    }


    //  /**
    //   * Verifies that two indicators have the same size and values to an offset
    //   *
    //   * @param expected indicator of expected values
    //   * @param actual indicator of actual values
    //   */
    //  public static void assertIndicatorEquals(
    //      MarketEventTestContext context,
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
     * Verifies that the actual `Num` value is not equal to the given
     * `String` representation.
     *
     * @param actual the actual `Num` value
     * @param expected the given `String` representation to compare the actual
     * value to
     *
     * @throws AssertionError if the actual value is equal to the given
     * `String` representation
     */
    @JvmStatic
    fun assertNumNotEquals(expected: String, actual: Num) {
        Assert.assertNotEquals(actual.numFactory.numOf(BigDecimal(expected)), actual)
    }


    /**
     * Verifies that the actual `Num` value is not equal to the given
     * `Num`.
     *
     * @param actual the actual `Num` value
     * @param expected the given `Num` representation to compare the actual
     * value to
     *
     * @throws AssertionError if the actual value is equal to the given `Num`
     * representation
     */
    @JvmStatic
    fun assertNumNotEquals(expected: Num?, actual: Num?) {
        Assert.assertNotEquals(expected, actual)
    }


    /**
     * Verifies that the actual `Num` value is not equal (within a positive
     * offset) to the given `double` representation.
     *
     * @param actual the actual `Num` value
     * @param expected the given `double` representation to compare the actual
     * value to
     *
     * @throws AssertionError if the actual value is equal to the given
     * `double` representation
     */
    @JvmStatic
    fun assertNumNotEquals(expected: Double, actual: Num) {
        Assert.assertNotEquals(expected, actual.doubleValue(), GENERAL_OFFSET)
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
    @JvmStatic
    fun assertUnstable(indicator: Indicator<*>) {
        Assert.assertFalse(indicator.isStable)
    }

    @JvmStatic
    fun assertStable(indicator: Indicator<*>) {
        Assert.assertTrue(indicator.isStable)
    }
}

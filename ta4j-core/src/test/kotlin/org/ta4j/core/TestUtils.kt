/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import java.math.BigDecimal
import org.assertj.core.api.Assertions
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertEquals(actual.numFactory.numOf(BigDecimal(expected)), actual)
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
        assertEquals(expected, actual)
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
        assertEquals(actual.numFactory.numOf(expected), actual)
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
        assertNotEquals(actual.numFactory.numOf(unexpected), actual)
    }


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
        assertNotEquals(actual.numFactory.numOf(BigDecimal(expected)), actual)
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
        assertNotEquals(expected, actual)
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
        assertNotEquals(expected, actual.doubleValue(), GENERAL_OFFSET)
    }


    @JvmStatic
    fun assertUnstable(indicator: Indicator<*>) {
        assertFalse(indicator.isStable)
    }

    @JvmStatic
    fun assertStable(indicator: Indicator<*>) {
        assertTrue(indicator.isStable)
    }
}

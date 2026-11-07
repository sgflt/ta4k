/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.num

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class DoubleNumTest {

    @Test
    fun testEqualsDoubleNumWithDecimalNum() {
        val decimalNum = DecimalNum.valueOf(3.0)
        val doubleNum = DoubleNum.valueOf(3.0)

        assertNotEquals(doubleNum, decimalNum)
    }

    @Test
    fun testZeroEquals() {
        val num1: Num = DoubleNum.valueOf(-0.0)
        val num2: Num = DoubleNum.valueOf(0.0)

        assertThat(num1).isEqualTo(num2)
    }
}
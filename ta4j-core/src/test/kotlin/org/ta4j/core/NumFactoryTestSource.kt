/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.num.DecimalNumFactory
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

/**
 * Test source providing different NumFactory implementations for parameterized tests.
 */
object NumFactoryTestSource {
    @JvmStatic
    fun numFactories(): List<NumFactory> {
        return listOf(DoubleNumFactory, DecimalNumFactory.getInstance())
    }
}

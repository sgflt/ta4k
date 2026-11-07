/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import java.util.stream.Stream
import org.ta4j.core.num.DecimalNumFactory
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.NumFactory

/**
 * Test source providing different NumFactory implementations for parameterized tests.
 */
object NumFactoryTestSource {
    @JvmStatic
    fun numFactories(): Stream<NumFactory> {
        return Stream.of(DoubleNumFactory, DecimalNumFactory.getInstance())
    }
}

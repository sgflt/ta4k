/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.bool

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.indicators.helpers.FixedBooleanIndicator

internal class ThresholdCombinationIndicatorTest {

    private fun context() = MarketEventTestContext().withCandlePrices(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)

    @Test
    fun wasTrueWithinRemembersLastTrueForBarCount() {
        val source = FixedBooleanIndicator(true, false, false, false, true, false, false)
        context().withIndicator(source.wasTrueWithin(3))
            .assertNextTrue()
            .assertIsUnStable()
            .assertNextTrue()
            .assertNextTrue()
            .assertIsStable()
            .assertNextFalse()
            .assertNextTrue()
            .assertNextTrue()
            .assertNextTrue()
    }

    @Test
    fun wasTrueWithinOneBarFollowsSource() {
        val source = FixedBooleanIndicator(false, true, false)
        context().withIndicator(source.wasTrueWithin(1))
            .assertNextFalse()
            .assertNextTrue()
            .assertNextFalse()
    }

    @Test
    fun andWithinNeedsBothInWindowNotOnSameBar() {
        val first = FixedBooleanIndicator(true, false, false, false, false, false, false)
        val second = FixedBooleanIndicator(false, false, true, false, false, false, true)
        context().withIndicator(first.andWithin(second, 3))
            .assertNextFalse()
            .assertNextFalse()
            .assertNextTrue() // first on bar 1, second on bar 3
            .assertNextFalse() // first left the window
            .assertNextFalse()
            .assertNextFalse()
            .assertNextFalse()
    }

    @Test
    fun orWithinNeedsEitherInWindow() {
        val first = FixedBooleanIndicator(true, false, false, false, false, false, false)
        val second = FixedBooleanIndicator(false, false, false, true, false, false, false)
        context().withIndicator(first.orWithin(second, 2))
            .assertNextTrue()
            .assertNextTrue()
            .assertNextFalse()
            .assertNextTrue()
            .assertNextTrue()
            .assertNextFalse()
    }

    @Test
    fun rejectsNonPositiveBarCount() {
        assertThatThrownBy { FixedBooleanIndicator(true).wasTrueWithin(0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}

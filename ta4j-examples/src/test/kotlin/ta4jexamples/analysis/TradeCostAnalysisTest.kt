/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.analysis

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

class TradeCostAnalysisTest {

    @Test
    fun test() {
        assertDoesNotThrow {
            TradeCostAnalysis.main(emptyArray())
        }
    }
}
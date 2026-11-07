/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.indicators

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ta4jexamples.loaders.MockMarketEventsLoader

class IndicatorsToChartTest {

    @Test
    fun testIndicatorsToChartMain() {
        // Test chart creation without displaying GUI (avoid headless issues)
        val chartGenerator = IndicatorsToChart()
        val marketEvents = MockMarketEventsLoader.loadMarketEvents()

        chartGenerator.use { generator ->
            marketEvents.forEach { event ->
                generator.processMarketEvent(event)
            }
            
            // Test chart creation instead of display
            val chart = generator.createChart()
            assertThat(chart).isNotNull()
            assertThat(chart.title.text).contains("Technical Analysis")
        }
    }
}

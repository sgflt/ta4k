/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.events.MarketEvent
import org.ta4j.core.mocks.MockIndicator

interface ExternalIndicatorTest {

    fun getMarketEvents(): List<MarketEvent>

    fun getIndicator(vararg params: Any): MockIndicator
}
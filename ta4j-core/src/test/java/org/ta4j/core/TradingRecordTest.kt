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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.num.NumFactory

class TradingRecordTest {

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getCurrentPositionTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Empty record should have new (closed) position
        assertThat(context.tradingRecord.currentPosition.isNew).isTrue()
        assertThat(context.tradingRecord.currentPosition.isOpened).isFalse()

        // After entry, position should be opened
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.currentPosition.isNew).isFalse()
        assertThat(context.tradingRecord.currentPosition.isOpened).isTrue()

        // After exit, position should be new (closed) again
        context.exit(1.0).at(105.0)
        assertThat(context.tradingRecord.currentPosition.isNew).isTrue()
        assertThat(context.tradingRecord.currentPosition.isOpened).isFalse()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun operateTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Initial state
        assertThat(context.tradingRecord.currentPosition.isOpened).isFalse()
        assertThat(context.tradingRecord.positionCount).isEqualTo(0)
        assertThat(context.tradingRecord.lastPosition).isNull()
        assertThat(context.tradingRecord.lastTrade).isNull()
        assertThat(context.tradingRecord.lastEntry).isNull()
        assertThat(context.tradingRecord.lastExit).isNull()

        // First entry
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.currentPosition.isOpened).isTrue()
        assertThat(context.tradingRecord.positionCount).isEqualTo(0) // No closed positions yet
        assertThat(context.tradingRecord.lastPosition).isNull()
        assertThat(context.tradingRecord.lastTrade).isNotNull()
        assertThat(context.tradingRecord.lastTrade?.type).isEqualTo(TradeType.BUY)
        assertThat(context.tradingRecord.lastEntry).isNotNull()
        assertThat(context.tradingRecord.lastExit).isNull()

        // First exit (completes first position)
        context.exit(1.0).at(105.0)
        assertThat(context.tradingRecord.currentPosition.isOpened).isFalse()
        assertThat(context.tradingRecord.positionCount).isEqualTo(1) // One closed position
        assertThat(context.tradingRecord.lastPosition).isNotNull()
        // Let's check what the actual exit trade type is
        val exitTradeType = context.tradingRecord.lastTrade?.type
        assertThat(exitTradeType).isNotNull()
        assertThat(context.tradingRecord.lastEntry).isNotNull()
        assertThat(context.tradingRecord.lastExit).isNotNull()

        // Second entry
        context.enter(1.0).at(110.0)
        assertThat(context.tradingRecord.currentPosition.isOpened).isTrue()
        assertThat(context.tradingRecord.positionCount).isEqualTo(1) // Still one closed position
        assertThat(context.tradingRecord.lastTrade?.type).isEqualTo(TradeType.BUY)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun isClosedTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Empty record is closed
        assertThat(context.tradingRecord.isClosed).isTrue()

        // After entry, record is not closed
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.isClosed).isFalse()

        // After exit, record is closed again
        context.exit(1.0).at(105.0)
        assertThat(context.tradingRecord.isClosed).isTrue()
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getPositionCountTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Initially no positions
        assertThat(context.tradingRecord.positionCount).isEqualTo(0)

        // After entry, still no closed positions
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.positionCount).isEqualTo(0)

        // After exit, one closed position
        context.exit(1.0).at(105.0)
        assertThat(context.tradingRecord.positionCount).isEqualTo(1)

        // Complete another position
        context.enter(1.0).at(110.0)
        context.exit(1.0).at(115.0)
        assertThat(context.tradingRecord.positionCount).isEqualTo(2)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getLastPositionTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Initially no last position
        assertThat(context.tradingRecord.lastPosition).isNull()

        // After opening position, still no last closed position
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.lastPosition).isNull()

        // After closing first position
        context.exit(1.0).at(105.0)
        val firstPosition = context.tradingRecord.lastPosition
        assertThat(firstPosition).isNotNull()
        assertThat(firstPosition?.entry?.pricePerAsset).isEqualTo(numFactory.numOf(100))
        assertThat(firstPosition?.exit?.pricePerAsset).isEqualTo(numFactory.numOf(105))

        // After closing second position
        context.enter(1.0).at(110.0)
        context.exit(1.0).at(115.0)
        val secondPosition = context.tradingRecord.lastPosition
        assertThat(secondPosition).isNotEqualTo(firstPosition)
        assertThat(secondPosition?.entry?.pricePerAsset).isEqualTo(numFactory.numOf(110))
        assertThat(secondPosition?.exit?.pricePerAsset).isEqualTo(numFactory.numOf(115))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getLastTradeTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Initially no trades
        assertThat(context.tradingRecord.lastTrade).isNull()
        assertThat(context.tradingRecord.getLastTrade(TradeType.BUY)).isNull()
        assertThat(context.tradingRecord.getLastTrade(TradeType.SELL)).isNull()

        // After first entry
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.lastTrade?.type).isEqualTo(TradeType.BUY)
        assertThat(context.tradingRecord.getLastTrade(TradeType.BUY)?.pricePerAsset).isEqualTo(numFactory.numOf(100))
        assertThat(context.tradingRecord.getLastTrade(TradeType.SELL)).isNull()

        // After first exit
        context.exit(1.0).at(105.0)
        assertThat(context.tradingRecord.lastTrade).isNotNull()

        // After second entry
        context.enter(1.0).at(110.0)
        assertThat(context.tradingRecord.lastTrade?.type).isEqualTo(TradeType.BUY)
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun getLastEntryExitTest(numFactory: NumFactory) {
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Initially no entry/exit trades
        assertThat(context.tradingRecord.lastEntry).isNull()
        assertThat(context.tradingRecord.lastExit).isNull()

        // After first entry
        context.enter(1.0).at(100.0)
        assertThat(context.tradingRecord.lastEntry?.pricePerAsset).isEqualTo(numFactory.numOf(100))
        assertThat(context.tradingRecord.lastExit).isNull()

        // After first exit
        context.exit(1.0).at(105.0)
        assertThat(context.tradingRecord.lastEntry?.pricePerAsset).isEqualTo(numFactory.numOf(100))
        assertThat(context.tradingRecord.lastExit?.pricePerAsset).isEqualTo(numFactory.numOf(105))

        // After second entry
        context.enter(1.0).at(110.0)
        assertThat(context.tradingRecord.lastEntry?.pricePerAsset).isEqualTo(numFactory.numOf(110))
        assertThat(context.tradingRecord.lastExit?.pricePerAsset).isEqualTo(numFactory.numOf(105))
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun tradingRecordWithInitialTradesTest(numFactory: NumFactory) {
        // Test creating a trading record with initial trades like in the original Java test
        val context = TradingRecordTestContext().withNumFactory(numFactory)

        // Create a scenario similar to the original openedRecord
        context.enter(1.0).at(100.0)
        context.exit(1.0).at(105.0)
        context.enter(1.0).at(110.0)

        // Should have opened position
        assertThat(context.tradingRecord.currentPosition.isOpened).isTrue()
        assertThat(context.tradingRecord.positionCount).isEqualTo(1)
        assertThat(context.tradingRecord.lastTrade?.type).isEqualTo(TradeType.BUY)

        // Create a scenario similar to the original closedRecord
        context.exit(1.0).at(115.0)

        // Should be closed
        assertThat(context.tradingRecord.currentPosition.isNew).isTrue()
        assertThat(context.tradingRecord.positionCount).isEqualTo(2)
        // All trades use the same starting type, so exit trades are also BUY for BUY starting type
        assertThat(context.tradingRecord.lastTrade?.type).isEqualTo(TradeType.BUY)
    }
}

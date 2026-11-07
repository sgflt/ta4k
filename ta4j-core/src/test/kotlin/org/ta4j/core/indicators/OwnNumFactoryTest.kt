/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators

import java.time.Duration
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.Indicators
import org.ta4j.core.events.CandleReceived
import org.ta4j.core.indicators.bool.chandelier.ChandelierExitLongIndicator
import org.ta4j.core.indicators.bool.chandelier.ChandelierExitShortIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.average.VWMAIndicator
import org.ta4j.core.indicators.numeric.candles.DojiIndicator
import org.ta4j.core.indicators.numeric.candles.ThreeBlackCrowsIndicator
import org.ta4j.core.indicators.numeric.channels.keltner.KeltnerChannelFacade
import org.ta4j.core.indicators.numeric.channels.keltner.KeltnerChannelLowerIndicator
import org.ta4j.core.indicators.numeric.channels.keltner.KeltnerChannelMiddleIndicator
import org.ta4j.core.indicators.numeric.momentum.adx.ADXIndicator
import org.ta4j.core.indicators.numeric.oscillators.AccelerationDecelerationIndicator
import org.ta4j.core.indicators.numeric.oscillators.AwesomeOscillatorIndicator
import org.ta4j.core.indicators.numeric.oscillators.StochasticOscillatorKIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonDownIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonUpIndicator
import org.ta4j.core.num.NumFactory

/**
 * Indicators must build their inner indicators from their own NumFactory; mixing Num types throws.
 */
internal class OwnNumFactoryTest {

    private val events = (0 until 40).map { i ->
        val close = 50.0 + 5 * kotlin.math.sin(i / 3.0)
        val begin = Instant.EPOCH.plus(Duration.ofDays(i.toLong()))
        CandleReceived(
            timeFrame = TimeFrame.DAY,
            beginTime = begin,
            endTime = begin.plus(Duration.ofDays(1)),
            openPrice = close - 0.3,
            highPrice = close + 1,
            lowPrice = close - 1,
            closePrice = close,
            volume = 100.0 + i,
        )
    }

    @ParameterizedTest
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun indicatorsUseTheirOwnNumFactory(numFactory: NumFactory) {
        val keltner = KeltnerChannelFacade(5, 5, 2, numFactory)
        val middle = KeltnerChannelMiddleIndicator(numFactory, barCountEMA = 5)
        val numeric: List<NumericIndicator> = listOf(
            StochasticOscillatorKIndicator(numFactory, 5),
            AroonUpIndicator(numFactory, barCount = 5),
            AroonDownIndicator(numFactory, 5),
            middle,
            KeltnerChannelLowerIndicator(numFactory, middle, 2.0, 5),
            keltner.upper(),
            AwesomeOscillatorIndicator(numFactory, shortBarCount = 3, longBarCount = 8),
            AccelerationDecelerationIndicator(numFactory, 3, 8),
            VWMAIndicator(numFactory, Indicators.extended(numFactory).closePrice(), 5),
            ADXIndicator(numFactory, 5, 5),
        )
        val booleans: List<Indicator<Boolean>> = listOf(
            ThreeBlackCrowsIndicator(numFactory, 5, 0.3),
            DojiIndicator(numFactory, 5, 0.1),
            ChandelierExitLongIndicator(numFactory, 5, 3.0),
            ChandelierExitShortIndicator(numFactory, 5, 3.0),
        )
        MarketEventTestContext()
            .withNumFactory(numFactory)
            .withMarketEvents(events)
            .withIndicators(*(numeric + booleans).toTypedArray())
            .fastForward(events.size)

        val expectedType = numFactory.one().javaClass
        numeric.forEach { indicator ->
            assertThat(indicator.value.javaClass).describedAs(indicator.toString()).isEqualTo(expectedType)
        }
    }
}
